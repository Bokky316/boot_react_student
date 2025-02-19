package com.javalab.student.dto;

import com.javalab.student.constant.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;            // 주문번호
    private Long member_id;     // 주문ID
    private String name;        // 주문자명
    private LocalDateTime orderDate;// 주문일
    private OrderStatus orderStatus; // 주문상태
    private Long amount;        // 주문금액
    private String waybillNum;  // 운송장번호
    private String parcelCd;    // 택배사코드
}
