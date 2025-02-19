package com.javalab.student.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Slf4j
public class PortOneRestTemplateTest {

    @Autowired
    private RestTemplate restTemplate;

    // ✅ 포트원(아임포트) API 키 & 시크릿
    private final String IMP_KEY = "6738525558640373";  // ✅ 테스트용 아임포트 API 키(본인의 API 키로 변경 필요)
    // ✅ 포트원 API 시크릿 키
    private final String IMP_SECRET = "VG95in0lolTTHB13ZJKxS4iK2Iw4XY1YMWkq78jpplXAoYLi73hzwiqxxozOHsN12R8c6VbvfrT6HyRz"; // ✅ 테스트용 아임포트 API 시크릿(본인의 API 시크릿으로 변경 필요)

    // ✅ 프론트엔드에서 받은 결제 UID, 거래할 때마다 다름 (거래 검증용)
    private final String IMP_UID = "imp_112920710163";  // ✅ 테스트용 결제 UID(본인의 결제 UID로 변경 필요)

    /**
     * 1. 포트원(아임포트) API에서 Access Token 발급 테스트
     */
    private String getAccessToken() throws Exception {
        String url = "https://api.iamport.kr/users/getToken";

        // ✅ 1. 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

        // ✅ 2. 요청 바디 데이터 생성
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("imp_key", IMP_KEY);
        requestBody.put("imp_secret", IMP_SECRET);

        // ✅ 3. JSON 변환 수행 (ObjectMapper 활용)
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequest = objectMapper.writeValueAsString(requestBody);

        // ✅ 4. HTTP 요청 실행
        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        // ✅ 5. 응답 확인 및 로그 출력
        log.info("📌 [액세스 토큰 요청] 응답 코드: {}", response.getStatusCode());
        log.info("📌 [액세스 토큰 요청] 응답 본문: {}", response.getBody());

        // ✅ 6. 응답 검증 (200 OK)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // ✅ 7. 응답에서 access_token 추출
        JsonNode rootNode = objectMapper.readTree(response.getBody());
        return rootNode.path("response").path("access_token").asText();
    }

    /**
     * 2. 결제 검증 테스트 (포트원 서버에서 결제 상태 확인)
     */
    @Test
    public void testVerifyPayment() throws Exception {
        // ✅ 1. 액세스 토큰 발급
        String accessToken = getAccessToken();

        // ✅ 2. 결제 검증 API 호출
        String url = "https://api.iamport.kr/payments/" + IMP_UID;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // ✅ 3. 응답 확인 및 로그 출력
        log.info("📌 [결제 검증 요청] 응답 코드: {}", response.getStatusCode());
        log.info("📌 [결제 검증 요청] 응답 본문: {}", response.getBody());

        // ✅ 4. 응답 검증 (200 OK)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // ✅ 5. 응답 데이터에서 결제 상태 확인
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response.getBody());
        String status = rootNode.path("response").path("status").asText();
        int amount = rootNode.path("response").path("amount").asInt();

        assertThat(status).isEqualTo("paid");  // 결제 상태가 "paid"인지 확인
        assertThat(amount).isEqualTo(20002);   // 결제 금액이 올바른지 확인
    }
}
