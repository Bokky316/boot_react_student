package com.javalab.student.dto;

import com.javalab.student.constant.OrderStatus;
import lombok.*;

import java.math.BigDecimal;

/**
 * 결제 요청 DTO
 * - 결제 요청 시 프론트엔드에서 전달받는 데이터를 담는 DTO
 * - 담아진 데이터는 컨트롤러 레이어에서 결제 요청을 위한 서비스로 전달된다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {
    private String impUid;       // 포트원 결제 처리 번호
    private Long merchantUid;    // 주문번호(결제 전에 생성된 주문ID)
    private String name;         // 상품명
    private BigDecimal paidAmount; // 💡 변경: 결제 금액을 BigDecimal로 일관성 유지
    private String payMethod;    // 결제 방식 추가
    private String pgProvider;   // PG사 정보 추가
    private OrderStatus orderStatus; // 💡 추가: 결제 상태 (PAYMENT_COMPLETED, PENDING, CANCELLED 등)
    private String buyerEmail;   // 구매자 이메일
    private String buyerName;    // 구매자 이름
    private String buyerTel;     // 구매자 연락처
    private String buyerAddr;    // 배송지 주소
    private String buyerPostcode; // 우편번호
    private Long paidAt;          // 💡 Unix Timestamp 유지
}
