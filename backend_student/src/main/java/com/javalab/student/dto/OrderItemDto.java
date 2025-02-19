package com.javalab.student.dto;

import com.javalab.student.entity.OrderItem;
import lombok.*;

/**
 * 한 건의 주문 Item(상품) 정보를 화면에 보내는 역할
 */
@Getter @Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemDto {
    private Long orderItemId;       // 주문아이템 id
    private Long itemId;            // 상품ID
    private String itemNm;          // 상품명
    private Integer count;          // 주문 수량
    private Long orderPrice;        // 주문 금액
    private String imgUrl;          // 상품 이미지 경로

    /**
     * 생성자
     * - 엔티티를 받아서 엔티티의 값을 자신(Dto)의 멤버 변수에 할당함.
     *   즉, Entity -> Dto 변환
     */
    public OrderItemDto(OrderItem orderItem, String imgUrl){
        // 주문 아이템 id
        this.orderItemId = orderItem.getId();
        // 상품아이디(item id)
        this.itemId = orderItem.getItem().getId();
        // 주문 상품의 이름
        this.itemNm = orderItem.getItem().getItemNm();
        // 주문 수량
        this.count = orderItem.getCount();
        // 주문 금액
        this.orderPrice = orderItem.getOrderPrice();
        // 대표이미지 경로
        this.imgUrl = imgUrl;
    }


}