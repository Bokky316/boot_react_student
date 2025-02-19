package com.javalab.student.service;

import com.javalab.student.dto.ChatMessageDto;
import com.javalab.student.entity.ChatMessage;
import com.javalab.student.entity.ChatRoom;
import com.javalab.student.entity.Member;
import com.javalab.student.repository.ChatMessageRepository;
import com.javalab.student.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 🔹 메시지를 저장하고 WebSocket을 통해 채팅방에 전달
     */
    @Transactional
    public void saveAndSendMessage(ChatMessageDto messageDto) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        // 메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(Member.builder().id(messageDto.getSenderId()).build())
                .content(messageDto.getContent())
                .sentAt(LocalDateTime.now())
                .build();

        chatMessageRepository.save(chatMessage);

        // WebSocket을 통해 채팅방의 모든 참여자에게 메시지 전송
        messagingTemplate.convertAndSend("/topic/chat/" + messageDto.getChatRoomId(), messageDto);
    }


    /**
     * 🔹 특정 채팅방의 메시지 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessagesByChatRoom(Long chatRoomId) {
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);

        return messages.stream().map(msg -> new ChatMessageDto(
                msg.getId(),
                msg.getContent(),
                msg.getChatRoom().getId(),
                msg.getSender().getId(),
                msg.getSentAt(),
                msg.isRead()
        )).collect(Collectors.toList());
    }








}
