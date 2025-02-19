package com.javalab.student.entity;

import jakarta.persistence.*;
import lombok.*;
@Table(name="item_img")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemImg implements Comparable<ItemImg>{

    @Id
    private String uuid;

    private String fileName; // 파일명
    private String repimgYn; // 대표 이미지 여부
    private int ord; // 순서
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id") // 다른 데이터베이스에 적용시 주석해제하고 테스트할것.
    private Item item;
    @Override
    public int compareTo(ItemImg other) {
        return this.ord - other.ord;
    }
    public void changeBoard(Item item) {
        this.item = item;
    }

}