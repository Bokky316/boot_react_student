package com.javalab.student.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ChatRoomResponseDto {
    private Long id;           // 채팅방 ID
    private String name;       // 채팅방 이름
    private LocalDateTime createdAt; // 채팅방 개설일자
    private Long ownerId;      // 방 개설자 ID
    private String ownerName;  // 방 개설자 이름
    private String status;     // 참여 상태 ("JOINED" or "PENDING")
}
