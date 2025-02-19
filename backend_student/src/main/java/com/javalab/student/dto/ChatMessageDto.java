package com.javalab.student.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private Long id;           // 메시지 ID
    private String content;    // 메시지 내용
    private Long chatRoomId;   // 채팅방 ID
    private Long senderId;     // 발신자 ID
    private LocalDateTime sentAt; // 메시지 전송 시간
    private boolean isRead;    // 메시지 읽음 여부
}
