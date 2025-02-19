package com.javalab.student.service;

import com.javalab.student.entity.Member;
import com.javalab.student.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Redis 서비스 클래스 (권한 캐싱 전용)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, Object> redisObjectTemplate;  // ✅ 권한 캐싱용
    private final MemberRepository memberRepository;

    /**
     * ✅ 사용자의 권한 정보를 Redis에 캐싱
     */
    public void cacheUserAuthorities(String email) {
        log.info("사용자 [{}]의 권한 정보를 Redis에 캐싱합니다.", email);

        // 1. 데이터베이스에서 사용자 조회
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("해당 이메일을 가진 사용자가 존재하지 않습니다.");
        }

        // 2. 사용자의 권한 정보를 SimpleGrantedAuthority 리스트로 변환
        List<String> authorities = member.getAuthorities().stream()
                .map(role -> role.getAuthority())
                .collect(Collectors.toList());

        // 3. Redis에 JSON 직렬화하여 저장 (Key: "AUTH:사용자이메일", Value: 권한 리스트, 유효시간: 6시간)
        redisObjectTemplate.opsForValue().set("AUTH:" + email, authorities, Duration.ofHours(6));

        log.info("사용자 [{}]의 권한 정보가 Redis에 저장되었습니다: {}", email, authorities);
    }

    /**
     * ✅ Redis에서 사용자의 권한 정보 조회
     */
    public List<String> getUserAuthoritiesFromCache(String email) {
        Object data = redisObjectTemplate.opsForValue().get("AUTH:" + email);

        if (data instanceof List<?>) {
            return ((List<?>) data).stream()
                    .map(String.class::cast)
                    .collect(Collectors.toList());
        }

        log.warn("Redis에서 [{}]의 권한 정보를 불러올 수 없습니다.", email);
        return List.of(); // 빈 리스트 반환
    }

    /**
     * ✅ Redis에서 사용자 권한 정보 삭제
     */
    public void removeUserAuthorities(String email) {
        redisObjectTemplate.delete("AUTH:" + email);
        log.info("사용자 [{}]의 권한 정보가 Redis에서 삭제되었습니다.", email);
    }
}
