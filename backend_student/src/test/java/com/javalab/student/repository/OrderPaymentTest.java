package com.javalab.student.repository;

import com.javalab.student.constant.ItemSellStatus;
import com.javalab.student.entity.Item;
import com.javalab.student.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Slf4j
public class OrderPaymentTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Member testMember;
    private Item testItem;



    @Test
    @Commit
    void createItemTest() {
        // Given
        Item newItem = Item.builder()
                .itemNm("New Test Item")
                .price(2000L)
                .stockNumber(20)
                .itemDetail("This is a new test item.")
                .receivedDate(LocalDateTime.now())
                .itemSellStatus(ItemSellStatus.SELL)
                .build();

        // When
        Item savedItem = itemRepository.save(newItem);

        // Then
        Item foundItem = itemRepository.findById(savedItem.getId()).orElse(null);
        assertNotNull(foundItem);
        assertEquals(newItem.getItemNm(), foundItem.getItemNm());
        assertEquals(newItem.getPrice(), foundItem.getPrice());
        assertEquals(newItem.getStockNumber(), foundItem.getStockNumber());
        assertEquals(newItem.getItemDetail(), foundItem.getItemDetail());
        assertEquals(newItem.getItemSellStatus(), foundItem.getItemSellStatus());
        assertEquals(newItem.getReceivedDate(), foundItem.getReceivedDate());
    }
}
