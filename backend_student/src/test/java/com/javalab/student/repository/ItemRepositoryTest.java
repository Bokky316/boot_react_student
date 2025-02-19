package com.javalab.student.repository;

import com.javalab.student.constant.ItemSellStatus;
import com.javalab.student.entity.Item;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Slf4j
public class ItemRepositoryTest {
    @Autowired
    ItemRepository itemRepository;

    @Test
    @DisplayName("상품 저장 테스트")
    @Commit // 테스트 성공후 주석 풀어서 데이터베이스에 반영
    public void createItemsTest() {
        IntStream.rangeClosed(1, 10)
                .forEach(i -> {
                    Item item = Item.builder()
                            .itemNm("테스트 상품 " + i)
                            .price(10000L + i)
                            .itemDetail("테스트 상품 상세 설명 " + i)
                            .itemSellStatus(ItemSellStatus.SELL)
                            .stockNumber(10+i)
                            .receivedDate(LocalDateTime.now())
                            .build();

                    Item savedItem = itemRepository.save(item);
                    log.info(savedItem.toString());
                });
    }

}
