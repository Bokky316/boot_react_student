package com.javalab.student.controller;

import com.javalab.student.config.portone.PortOneProperties;
import com.javalab.student.dto.PaymentRequestDto;
import com.javalab.student.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 결제 관련 API 컨트롤러
 * - 1차 결제 요청 후 2차 검증을 수행하고 DB에 저장
 * - 포트원의 API Key와 Merchant UID를 백엔드에서 관리하여 보안 강화
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final PortOneProperties portOneProperties;

    /**
     * 프론트엔드에서 포트원 가맹점 UID를 조회할 수 있도록 제공
     * - 프론트엔드는 .env가 아니라 서버에서 merchantId를 가져가도록 한다.
     * @return merchantUid 정보
     */
    @GetMapping("/merchant-id")
    public ResponseEntity<Map<String, String>> getMerchantId() {
        log.info("가맹점 UID 조회 요청");
        return ResponseEntity.ok(Map.of("merchantId", portOneProperties.getMerchantUid()));
    }

    /**
     * 결제 요청 및 2차 검증 후 데이터 저장
     * - 프론트엔드에서 받은 결제 정보를 검증한 후, DB에 저장
     * - 클라이언트가 따로 verifyPayment를 호출할 필요 없음.
     * @param requestDto 결제 요청 정보 (상품명, 금액, 사용자 정보 포함)
     * @return 결제 요청 결과
     */
    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody PaymentRequestDto requestDto) {
        log.info("결제 요청 시작 - 주문 정보: {}", requestDto);

        Map<String, Object> response = paymentService.processPayment(requestDto);
        log.info("결제 처리 완료: {}", response);

        return ResponseEntity.ok(response);
    }
}
