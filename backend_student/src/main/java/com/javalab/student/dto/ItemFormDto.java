package com.javalab.student.dto;

import com.javalab.student.constant.ItemSellStatus;
import com.javalab.student.entity.Item;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/***********************************************
 * 화면에서 입력한 Item(상품)정보를 받아주는 역할.
 * 엔티티 정보를 담아서 화면에 출력해주는 역할.
 **********************************************/
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Log4j2
public class ItemFormDto {

    private Long id;    // 상품 아이디

    @NotBlank(message = "상품명은 필수 입력 값입니다.")
    private String itemNm;

    @NotNull(message = "가격은 필수 입력 값입니다.")
    private Long price;

    @NotBlank(message = "상품 상세는 필수 입력 값입니다.")
    private String itemDetail;

    @NotNull(message = "재고는 필수 입력 값입니다.")
    private Integer stockNumber;

    @NotNull(message = "상품 입고일은 필수 입력 값입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime receivedDate; // 상품 입고일

    @NotNull(message = "상품 판매상태는 필수 입력 값입니다.")
    private ItemSellStatus itemSellStatus; // 상품 판매상태

    //첨부파일의 이름들
    private List<String> fileNames;

    private static ModelMapper modelMapper = new ModelMapper();


    /**
     * 생성자
     *  - 이 생성자가 있어야 다음과 같은 형태로 쿼리 결과 바인딩 가능.
     *    SELECT new com.javalab.boot.dto.ItemFormDTO(.....)
     *  - 파라미터의 순서와 타입이 select 문과 다르면 바인딩 안됨.
     */
    public ItemFormDto(Long id,
                       String itemNm,
                       String itemDetail,
                       Long price,
                       Integer stockNumber,
                       LocalDateTime receivedDate) {
        this.id = id;
        this.itemNm = itemNm;
        this.itemDetail = itemDetail;
        this.price = price;
        this.stockNumber =stockNumber;
        this.receivedDate = receivedDate;
    }

    public ItemFormDto(Item item) {
        this.id = item.getId();
        this.itemNm = item.getItemNm();
        this.itemDetail = item.getItemDetail();
        this.price = item.getPrice();
        this.stockNumber = item.getStockNumber();
        this.receivedDate = item.getReceivedDate();
        this.itemSellStatus = item.getItemSellStatus();
        this.fileNames = item.getImageSet().stream()
                .map(img -> img.getUuid() + "_" + img.getFileName())
                .collect(Collectors.toList());
    }

}