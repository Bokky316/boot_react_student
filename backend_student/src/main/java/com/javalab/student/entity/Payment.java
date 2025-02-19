package com.javalab.student.entity;

import com.javalab.student.constant.OrderStatus;
import com.javalab.student.constant.PayStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 결제 엔티티 : Payment
 * - 포트원 결제와 연동되는 테이블
 */
@Table(name = "payment")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "order")
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    /**
     * 주문과 1:1 관계
     * - 하나의 결제는 하나의 주문과만 연결됨
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * 포트원 결제 고유번호
     * - PG사에서 결제 완료 시 발급됨
     * - 초기 주문 생성 시에는 null 가능
     */
    @Column(name = "imp_uid", unique = true, length = 100)
    private String impUid;

    @Column(name = "item_nm")
    private String itemNm;

    /**
     * 주문 상태 (PENDING -> PAYMENT_COMPLETED -> CANCELLED)
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PAYMENT_PENDING;  // 주문 상태
    private BigDecimal amount;  // 결제 금액
    private String paymentMethod;// 결제 수단
    private String buyerEmail;  // 구매자 이메일
    private String buyerName;   // 구매자 이름
    private String buyerTel;    // 구매자 연락처
    private String buyerAddr;   // 배송지 주소
    private String buyerPostcode; // 우편번호
    private Long  paidAt;           // 결제 시각
    @Enumerated(EnumType.STRING)
    @Column(name = "pay_status")
    private PayStatus payStatus;    // 결제 / 취소

}
