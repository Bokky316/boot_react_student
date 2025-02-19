package com.javalab.student.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javalab.student.config.portone.PortOneProperties;
import com.siot.IamportRestClient.IamportClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Slf4j
public class PortOneClientTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IamportClient iamportClient;

    @Autowired
    private PortOneProperties portOneProperties;

    /**
     * ✅ 포트원 결제 요청 및 검증 통합 테스트
     */
    @Test
    @DisplayName("포트원 결제 요청 및 검증 테스트")
    public void testCompletePaymentProcess() throws Exception {
        // ✅ 1. Access Token 발급
        String accessToken = getAccessToken();
        assertNotNull(accessToken, "Access Token should not be null");

        // ✅ 2. 결제 요청 데이터 생성
        String merchantUid = UUID.randomUUID().toString(); // 고유 주문번호 생성
        BigDecimal amount = BigDecimal.valueOf(15000); // 결제 금액

        // ✅ 3. 포트원 결제 요청 실행
        String impUid = requestPayment(merchantUid, amount, accessToken);
        assertNotNull(impUid, "imp_uid should not be null or empty");

        log.info("✅ 결제 요청 성공 - imp_uid: {}, 결제금액: {}", impUid, amount);

        // ✅ 4. 결제 검증 수행
        verifyPayment(impUid, amount, accessToken);
    }

    /**
     * ✅ 포트원 Access Token 발급
     */
    private String getAccessToken() {
        try {
            String accessToken = iamportClient.getAuth().getResponse().getToken();
            log.info("✅ Access Token 발급 성공: {}", accessToken);
            return accessToken;
        } catch (Exception e) {
            log.error("❌ Access Token 발급 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Access Token 발급 실패", e);
        }
    }

    /**
     * ✅ 포트원 결제 요청 (1차 요청)
     * - 결제 요청을 포트원에 보내고 imp_uid를 반환받음.
     */
//    private String requestPayment(String merchantUid, BigDecimal amount, String accessToken) {
//        String url = "https://api.iamport.kr/payments"; // ✅ **올바른 결제 요청 API 사용**
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth(accessToken);
//
//        // ✅ 요청 바디 설정
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("merchant_uid", merchantUid);
//        requestBody.put("amount", amount);
//        requestBody.put("name", "테스트 상품");
//        requestBody.put("pay_method", "card");
//        requestBody.put("buyer_email", "test@example.com");
//        requestBody.put("buyer_name", "홍길동");
//        requestBody.put("buyer_tel", "010-1234-5678");
//        requestBody.put("buyer_addr", "서울특별시 강남구");
//        requestBody.put("buyer_postcode", "12345");
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
//        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//
//        log.info("🔹 결제 요청 응답 코드: {}", response.getStatusCode());
//        assertTrue(response.getStatusCode().is2xxSuccessful(), "Payment request should be successful");
//
//        // ✅ 응답에서 imp_uid 추출
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode rootNode;
//        try {
//            String responseBody = response.getBody();
//            if (responseBody == null) {
//                throw new RuntimeException("응답 본문이 null입니다.");
//            }
//            rootNode = objectMapper.readTree(responseBody);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("JSON 파싱 오류: " + e.getMessage(), e);
//        }
//        JsonNode responseNode = rootNode.path("response");
//
//        String impUid = responseNode.path("imp_uid").asText();
//        assertNotNull(impUid, "imp_uid should not be null or empty");
//
//        log.info("✅ 포트원 결제 요청 완료 - imp_uid: {}", impUid);
//        return impUid;
//    }

    private String requestPayment(String merchantUid, BigDecimal amount, String accessToken) {
        String url = "https://api.iamport.kr/payments/prepare"; // ✅ 올바른 결제 준비 API

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        // ✅ 요청 바디 설정
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("merchant_uid", merchantUid);
        requestBody.put("amount", amount);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        log.info("🔹 결제 요청 응답 코드: {}", response.getStatusCode());
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Payment request should be successful");

        // ✅ 응답에서 imp_uid 추출
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            String responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("응답 본문이 null입니다.");
            }
            rootNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 오류: " + e.getMessage(), e);
        }
        JsonNode responseNode = rootNode.path("response");

        // ✅ 포트원 `prepare` API는 `imp_uid`를 반환하지 않음 → 직접 생성한 `merchantUid` 사용
        log.info("✅ 포트원 결제 준비 완료 - merchant_uid: {}", merchantUid);
        return merchantUid;
    }


    /**
     * ✅ 포트원 결제 검증 (2차 검증)
     * - imp_uid를 사용하여 결제 정보를 확인.
     */
    private void verifyPayment(String impUid, BigDecimal expectedAmount, String accessToken) throws Exception {
        if (impUid.isBlank()) {
            throw new IllegalArgumentException("❌ imp_uid가 빈 문자열입니다.");
        }

        String url = "https://api.iamport.kr/payments/" + impUid; // ✅ 결제 검증 API

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        log.info("🔹 결제 검증 응답 코드: {}", response.getStatusCode());
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Payment verification should be successful");

        // ✅ 응답 데이터 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response.getBody());
        JsonNode responseNode = rootNode.path("response");

        String status = responseNode.path("status").asText();
        BigDecimal paidAmount = new BigDecimal(responseNode.path("amount").asText());

        log.info("✅ 결제 검증 완료 - 상태: {}, 결제금액: {}", status, paidAmount);

        // ✅ 결제 상태 및 금액 검증
        assertThat(status).isEqualTo("paid");
        assertThat(paidAmount).isEqualByComparingTo(expectedAmount);
    }
}
