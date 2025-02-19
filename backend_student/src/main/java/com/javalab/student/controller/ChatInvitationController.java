package com.javalab.student.controller;

import com.javalab.student.service.ChatInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 채팅방 초대 관련 API 요청을 처리하는 컨트롤러
 * - 초대받은 채팅방 개수를 반환하는 API
 */
@RestController
@RequestMapping("/api/chat/invitation")
@RequiredArgsConstructor
public class ChatInvitationController {

    private final ChatInvitationService chatInvitationService;

    /**
     * 🔹 초대받은 채팅방 개수를 반환하는 API
     * - PENDING 상태인 초대만 포함
     */
    @GetMapping("/count/{memberId}")
    public ResponseEntity<Integer> getPendingInvitationCount(@PathVariable("memberId") Long memberId) {
        int count = chatInvitationService.getPendingInvitationCount(memberId);
        return ResponseEntity.ok(count);
    }
}
