package com.javalab.student.controller;

import com.javalab.student.dto.ChatRoomRequestDto;
import com.javalab.student.dto.ChatRoomResponseDto;
import com.javalab.student.entity.ChatRoom;
import com.javalab.student.service.ChatRoomService;
import com.javalab.student.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 채팅방 컨트롤러
 * - 채팅방 생성, 채팅방 목록 조회, 초대받은 채팅방 개수 조회 API를 제공한다.
 * - 채팅방 생성 API: 채팅방을 생성한다.
 * - 채팅방 목록 조회 API: 특정 회원이 참여한 채팅방 목록을 조회한다.
 * - 초대받은 채팅방 개수 조회 API: 특정 회원이 초대받은 채팅방의 개수를 조회한다.
 * - 채팅방 생성 API는 POST /api/chat/create로 요청한다.
 * - 채팅방 목록 조회 API는 GET /api/chat/rooms/{memberId}로 요청한다.
 * - 초대받은 채팅방 개수 조회 API는 GET /api/chat/invited/{memberId}로 요청한다.
 * - 채팅방 생성 API는 ChatRoom 객체를 요청 바디에 담아 요청한다.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final MessageService messageService;

    /**
     * 채팅방 생성 API
     * - 채팅방을 생성한다.
     * - 채팅방 이름, 방 개설자 ID, 초대받은 회원 ID를 요청 바디에 담아 요청한다.
     * - 채팅방 생성에 성공하면 생성된 채팅방 정보를 반환한다.
     */
    @PostMapping("/rooms/create")
    public ResponseEntity<?> createChatRoom(@RequestBody ChatRoomRequestDto request) {
        ChatRoom chatRoom = chatRoomService.createChatRoom(request.getName(), request.getOwnerId(), request.getInviteeId());
        return ResponseEntity.ok(chatRoom);
    }

    /**
     * 🔹 특정 회원이 개설한 채팅방 + 초대받은 채팅방 목록 조회
     * - 채팅방 ID, 채팅방 이름, 채팅방 개설일자, 방 개설자 ID, 방 개설자 이름, 참여 상태를 반환한다.
     * - 프론트엔드에서 "채팅하기" 컴포넌트에서 보여질 채팅방 목록을 조회한다.
     */
    @GetMapping("/rooms/{memberId}")
    public ResponseEntity<List<ChatRoomResponseDto>> getUserChatRooms(@PathVariable("memberId") Long memberId) {
        List<ChatRoomResponseDto> chatRooms = chatRoomService.getChatRoomAndInvitedByMemberId(memberId);
        return ResponseEntity.ok(chatRooms);
    }



}
