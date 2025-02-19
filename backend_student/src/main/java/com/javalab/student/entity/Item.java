package com.javalab.student.entity;


import com.javalab.student.constant.ItemSellStatus;
import com.javalab.student.dto.ItemFormDto;
import com.javalab.student.exception.OutOfStockException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name="item")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item extends BaseEntity {

    @Id
    @Column(name="item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;       //상품 코드

    @Column(nullable = false, length = 50)
    private String itemNm; //상품명

    @Column(name="price", nullable = false)
    private Long price; //가격

    @Column(nullable = false)
    private int stockNumber; //재고수량

    @Lob
    @Column(nullable = false)
    private String itemDetail; //상품 상세 설명

    @Column(nullable = false, columnDefinition = "DATE DEFAULT CURRENT_DATE")
    private LocalDateTime receivedDate ; // 상품 입고일(기본 현재일자 입력)

    @OneToMany(mappedBy = "item",
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    @Builder.Default
    @BatchSize(size = 20) // 이 조건 누락시 10개의 게시물마다 이미지 조회 쿼리 나감.(비효율적)
    private Set<ItemImg> imageSet = new HashSet<>();

    //상품판매상태
    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus;

    // 첨부 이미지 추가
    public void addImage(String uuid, String fileName, String repimgYn) {
        ItemImg itemImg = ItemImg.builder()
                .uuid(uuid)
                .fileName(fileName)
                .item(this)
                .repimgYn(repimgYn)
                .ord(imageSet.size())
                .build();
        imageSet.add(itemImg);
    }

    // 첨부 이미지 엔티티 삭제
    public void clearImages() {
        imageSet.forEach(itemImg -> itemImg.changeBoard(null));
        this.imageSet.clear();
    }

    // 재고 감소(출고)
    public void removeStock(int stockNumber){
        int restStock = this.stockNumber - stockNumber;
        if(restStock<0){
            throw new OutOfStockException("상품의 재고가 부족 합니다. (현재 재고 수량: " + this.stockNumber + ")");
        }
        this.stockNumber = restStock;
    }

    // 재고 증가(입고)
    public void addStock(int stockNumber){
        this.stockNumber += stockNumber;
    }

    // Entity -> Dto 변환
    public ItemFormDto entityToDto() {
        ItemFormDto itemFormDto = ItemFormDto.builder()
                .id(this.getId())
                .itemNm(this.getItemNm())
                .itemDetail(this.getItemDetail())
                .price(this.getPrice())
                .stockNumber(this.getStockNumber())
                .receivedDate(this.getReceivedDate())
                .build();

        // 데이터베이스에 받아온 이미지들을 Dto로 이동
        List<String> fileNames =
                this.getImageSet().stream().sorted().map(itemImg ->
                        itemImg.getUuid() + "_"
                                + itemImg.getFileName()
                ).collect(Collectors.toList());
        itemFormDto.setFileNames(fileNames);

        return itemFormDto;
    }
}