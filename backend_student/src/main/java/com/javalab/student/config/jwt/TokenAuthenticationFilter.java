package com.javalab.student.config.jwt;

import com.javalab.student.service.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 액세스 토큰 인증 필터
 * - Spring Security의 요청 필터로 동작한다.
 * - 요청마다 JWT 토큰을 검증하고 인증 객체를 SecurityContext에 저장하는 역할.
 * - 시큐리티의 정상적인 동작 보다 먼저 실행되어야 한다.
 *   그래서 SecurityConfig에서 addFilterBefore() 메소드로 등록한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final RedisService redisService;
    private final TokenProvider tokenProvider;
    private final static String COOKIE_NAME = "accToken"; // 쿠키 이름으로 토큰 가져옴

    /**
     * 사용자의 모든 요청을 가로채서 JWT 토큰을 검증하고 인증 객체를 SecurityContext에 저장
     * 1. 사용자가 어떤 자원을 요청했을 때 SecurityConfig에서 설정한 인가 권한을 확인해야 한다.
     *   인가 권한을 확인할 때 사용되는 것이 SecurityContext에 저장된 인증 객체이다. 거기에 권한이 있으니까.
     * 2. 알반적인 세션 기반 시큐리티 인증 매커니즘에서는 로그인을 할 때 스프링 시큐리티 필터 체인에 의해서
     *   인증객체가 만들어지고 그것이 SecurityContext에 저장된다. 그리고 그걸로 SecurityConfig 설정에
     *   의해서 인가 권한을 확인한다. 또한 SecurityContext가 세션에 저장되어 로그인 이후의 요청에서는
     *   세션에서 인증 객체를 꺼내서 사용한다.
     * 3. 반면에 JWT를 사용할 때는 사용자의 인가 정보가 세션에 보관되지 않기 때문에 요청할 때마다 인증 객체를
     *   만들어서 SecurityContext에 저장해야 한다. 그리고 그걸로 SecurityConfig의 인가 설정과 비교해서
     *   허가/거부를 결정해야 한다. 또한 현재의 필터인 TokenAuthenticationFilter 단계에서는 인증 객체가
     *   SecurityContext에 저장되어 있지 않다. 단지 사용자로 부터 받은 JWT토큰만 가지고 있다. 그래서
     *   여기서 권한정보를 추출하고 인증 객체를 만들어서 SecurityContext에 저장해야 한다. 그런데 매번
     *   이 작업을 하는 것은 상당히 번거롭기 때문에 로그인 시 사용자의 권한 정보를 Redis에 저장해두고
     *   사용자의 요청 때마다 Redis에서 꺼내서 권한을 확인하고 인증 객체를 만들어서 SecurityContext에
     *   저장하는 방식을 사용한다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        log.info("TokenAuthenticationFilter.doFilterInternal 시작 - 요청 URI: {}", request.getRequestURI());

        // Swagger 및 로그인/특정 경로는 필터를 타지 않고 건너뛰기, 왜냐하면 이 필터는 인증이 필요 없는 요청은 건너뛰어야 하기 때문
        String path = request.getRequestURI();
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") ||    // Swagger UI
            path.equals("/api/auth/login") || path.equals("/api/auth/userInfo") ||  // 로그인, 사용자 정보 조회
            path.equals("/api/members/register") || path.equals("/api/members/checkEmail") ||   // 회원가입, 이메일 중복 확인
            path.equals("/ping.js") ||  // WebSocket 요청
            path.startsWith("/ws") || path.startsWith("/ws/info") ||    // WebSocket 요청(/ws는 WebSocket 요청, /ws/info는 WebSocket 연결 정보, 핸드쉐이크)
            path.startsWith("/topic/chat/")) {  // WebSocket 요청

            filterChain.doFilter(request, response);
            return;
        }

        // ✅ 추가: 요청이 프록시(Nginx)에서 전달된 경우 처리(회원가입, 로그인 요청은 예외)
        // 사용자가 요청한 URL이 http://43.200.140.40/api/auth/login 와 같으면
        // 이 URL에서 요청 URI (Request URI)는 /api/auth/login
        String forwardedFor = request.getHeader("X-Forwarded-For"); // 프록시에서 전달된 요청인지 확인, 클라이언트의 IP 주소를 전달받은 경우, Nginx의 환경설정 파일에 proxy_set_header X-Forwarded-For $remote_addr; 추가해야 함
        if (forwardedFor != null) {
            String forwardedPath = request.getRequestURI();

            log.info("프록시에서 전달된 요청 - X-Forwarded-For: {}, 요청 URI: {}", forwardedFor, forwardedPath);

            if (forwardedPath.equals("/auth/login") || forwardedPath.equals("/members/register")) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 1. 쿠키에서 액세스 토큰 추출
        String token = extractTokenFromCookies(request.getCookies());
        log.info("TokenAuthenticationFilter에서 추출한 쿠키 토큰: {}", token);

        // 2. 토큰 검증
        if (token == null || !tokenProvider.validateToken(token)) {
            String errorMessage = token == null
                    ? "인증 토큰이 누락되었습니다."
                    : "액세스 토큰이 만료되었습니다.";
            handleUnauthorizedResponse(response, errorMessage);
            return;
        }

        // 3. 토큰에서 이메일 추출
        String email = tokenProvider.getEmailFromToken(token);
        log.info("토큰에서 추출한 이메일: {}", email);

        // 4. 위에서 추출한 이메일로 Redis에서 권한 정보 조회, 로그인 성공 후에 Redis에 저장해놓은 권한 정보를 가져온다.
        List<String> roles = redisService.getUserAuthoritiesFromCache(email);
        if (roles == null || roles.isEmpty()) {
            handleUnauthorizedResponse(response, "Redis에서 권한 정보를 찾을 수 없습니다.");
            return;
        }

        // 5. Redis 권한 정보로 인증 객체 생성, 인증 객체를 SecurityContext 세팅
        Set<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
        log.info("액세스 토큰에서 추출한 이메일과 Redis에서 조회한 권한 정보로 생성된 인증 객체: {}", auth);

        // 6. 인증 객체를 SecurityContext 세팅, 여기서 세팅해놓은 인증 객체가 SecurityConfig에서 인가 권한 확인에 사용된다.
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 7. 다음 필터로 요청 전달, 더이상 필터가 없으면 사용자 원하는 요청을 처리 즉, 컨트롤러 메서드 호출
        filterChain.doFilter(request, response);
    }

    /**
     * 쿠키에서 JWT 토큰 추출
     */
    private String extractTokenFromCookies(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    log.info("쿠키에서 토큰 추출: {}", cookie.getValue());
                    return cookie.getValue(); // 쿠키 값 반환
                }
            }
        }
        return null;
    }

    /**
     * 401 Unauthorized 응답 처리
     */
    private void handleUnauthorizedResponse(HttpServletResponse response, String errorMessage) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 상태 코드 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 사용자에게 명확한 오류 메시지 전달
        response.getWriter().write(String.format(
                "{\"error\":\"Unauthorized\",\"message\":\"%s\"}", errorMessage));
        log.warn("401 Unauthorized 응답 반환 - {}", errorMessage);
    }

}
