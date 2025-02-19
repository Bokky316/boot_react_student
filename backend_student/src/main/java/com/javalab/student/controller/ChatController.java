package com.javalab.student.controller;

import com.javalab.student.dto.ChatMessageDto;
import com.javalab.student.service.ChatMessageService;
import com.javalab.student.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 채팅 관련 요청을 처리하는 컨트롤러
 * - 채팅방 메시지 전송
 * - 채팅방 입장
 * - 채팅방 목록 조회
 * - 채팅방 메시지 전송
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;

    /**
     * 🔹 특정 채팅방으로 메시지를 전송
     */
    @MessageMapping("/chat/{chatRoomId}")
    public void sendMessage(@RequestBody ChatMessageDto messageDto, @PathVariable Long chatRoomId) {
        log.info("💬 메시지 수신 - 채팅방 ID: {}, 보낸이: {}, 내용: {}", chatRoomId, messageDto.getSenderId(), messageDto.getContent());

        // ✅ 서비스 계층을 통해 메시지 저장 및 WebSocket 전송
        chatMessageService.saveAndSendMessage(messageDto);
    }

    /**
     * 🔹 초대받은 사용자가 채팅방에 입장 (초대 상태 변경)
     * - 초대 상태를 PENDING → JOINED로 변경
     * - 프론트에서 호출하는 REST API
     * - invitationId : 채팅방 초대 ID, WebSocket으로 채팅방 입장 알림
     */
    @PostMapping("/api/chat/invitation/join/{invitationId}")
    public void joinChatRoom(@PathVariable Long invitationId) {
        chatRoomService.joinChatRoom(invitationId);
    }


    /**
     * 🔹 특정 채팅방의 메시지 목록 조회
     * - 채팅방 ID를 기반으로 메시지를 조회 (최신순 정렬)
     */
    @GetMapping("/api/chat/messages/{chatRoomId}")
    public List<ChatMessageDto> getChatMessages(@PathVariable("chatRoomId") Long chatRoomId) {
        return chatMessageService.getMessagesByChatRoom(chatRoomId);
    }

    /**
     * 🔹 메시지 전송
     * @param messageDto
     */
    @PostMapping("/api/chat/send")
    public void sendMessage(@RequestBody ChatMessageDto messageDto) {
        log.info("💬 메시지 수신 - 채팅방 ID: {}, 보낸이: {}, 내용: {}", messageDto.getChatRoomId(), messageDto.getSenderId(), messageDto.getContent());

        // 서비스 계층을 통해 메시지 저장 및 WebSocket 전송
        chatMessageService.saveAndSendMessage(messageDto);
    }

}
