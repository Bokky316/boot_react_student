package com.javalab.student.entity;

import com.javalab.student.constant.OrderStatus;
import com.javalab.student.dto.OrderDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "orderItems")
public class Order extends BaseEntity {

    // Order key
    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    //주문일
    @Column(nullable = false, columnDefinition = "DATE DEFAULT CURRENT_DATE")
    private LocalDateTime orderDate;

    //주문상태
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    //주문금액
    @Column(name = "order_amount")
    private Long amount;

    // 운송장번호
    @Column(name = "waybill_num")
    private String waybillNum;

    // 택배사 code
    @Column(name = "parcel_cd")
    private String parcelCd;

    /**
     * 주문Items(연관관계매핑 - OrderItem)
     *  Order 엔티티를 영속화할 때 OrderItem 엔티티도 자동으로
     * 영속화되도록 하려면, Order의 @OneToMany 관계에
     * cascade = CascadeType.ALL을 적용해야.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL
            , orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @BatchSize(size = 10) // CartItem을 즉시 로딩할 때, 지정된 크기만큼 미리 로딩합니다.
    private List<OrderItem> orderItems = new ArrayList<>();

    /**
     * 배송 주소 엔티티 연관관계 매핑
     *  - 하나의 주문은 단 하나의 배송 주소와 연관된다.
     *  - 연관관계의 주인이 아니므로 mappedBy 속성 사용 즉, Order는 Address를 참조만 한다.
     *  - 주문주소 테이블의 키가 Order 테이블에 외래키로 들어오지 않는다.
     *  - 주문 데이터가 변경되면 배송 주소 테이블에 그대로 반영된다. 즉 주문생성 -> 배송주소생성, 주문삭제 -> 배송주소 삭제됨.
     */
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Address address;

    /**
     * Payment 엔티티와 연관관계 매핑
     *  - 하나의 주문은 하나의 결제정보와 연관된다.
     *  - 연관관계의 주인이 아니므로 mappedBy 속성 사용 즉, Order는 Payment를 참조만 한다.
     *  - Payment 테이블의 키가 Order 테이블에 외래키로 들어오지 않는다.
     *  - 주문 데이터가 변경되면 Payment 테이블에 그대로 반영된다. 즉 주문삭제 -> Payment 삭제됨.
     */
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;


    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    /**
     * 주문등록 과정에서 주문Item으로 주문Entity 생성.
     * @param member
     * @param orderItemList
     */
    public static Order createOrder(Member member, List<OrderItem> orderItemList) {
        Order order = new Order();
        order.setMember(member);    // 주문자 정보 세팅

        long totalAmount = 0L;

        // 주문Item 갯수만큼 주문Item에 추가
        for(OrderItem orderItem : orderItemList) {
            order.addOrderItem(orderItem);
            totalAmount += orderItem.getOrderPrice() * orderItem.getCount();
        }
        // 총 주문금액 세팅
        order.setAmount(totalAmount);
        // 주문상태 세팅(ORDR01 : 주문)
        order.setOrderStatus(OrderStatus.ORDERED);

        // 주문일자를 오늘날짜로 세팅
        order.setOrderDate(LocalDateTime.now());

        return order;
    }

    public int getTotalPrice() {
        int totalPrice = 0;
        for(OrderItem orderItem : orderItems){
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }

    public void cancelOrder() {
        this.orderStatus = OrderStatus.CANCELED;
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    public OrderDto entityToDto(){
        return OrderDto.builder()
                .id(this.id)
                .member_id(this.member.getId())
                .orderDate(this.orderDate)
                .orderStatus(this.orderStatus)
                .amount(this.amount)
                .waybillNum(this.waybillNum)
                .parcelCd(this.parcelCd)
                .build();
    }
}