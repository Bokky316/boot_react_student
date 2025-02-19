package com.javalab.student.service;

import com.javalab.student.dto.ItemFormDto;
import com.javalab.student.entity.Item;
import com.javalab.student.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    // 상품 조회
    @Transactional(readOnly = true)
    public ItemFormDto getItemById(Long id) {
        Optional<Item> item = itemRepository.findById(id);
        if (item.isEmpty()) {
            throw new RuntimeException("해당 ID의 상품을 찾을 수 없습니다: " + id);
        }
        return new ItemFormDto(item.get());
    }
}
