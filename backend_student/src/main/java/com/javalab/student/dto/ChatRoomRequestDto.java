package com.javalab.student.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomRequestDto {
    private String name;
    private Long ownerId;
    private Long inviteeId;
}
