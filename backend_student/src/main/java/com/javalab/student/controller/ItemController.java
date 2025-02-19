package com.javalab.student.controller;

import com.javalab.student.dto.ItemFormDto;
import com.javalab.student.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;


    // 상품 정보 조회 API
    @GetMapping("/{id}")
    public ResponseEntity<ItemFormDto> getItemById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }
}
