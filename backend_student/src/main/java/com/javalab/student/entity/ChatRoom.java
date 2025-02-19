package com.javalab.student.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_room")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom extends BaseEntity {
    // 채팅방 식별자
    @Id
    @Column(name = "chat_room_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 채팅방 이름 (1:1 채팅이므로 참여자 이름 기반으로 생성)
    @Column(nullable = false)
    private String name;

    // 채팅방 메시지 (삭제 기능 없음)
    @OneToMany(mappedBy = "chatRoom",
                            cascade = CascadeType.ALL,
                            orphanRemoval = false)
    private List<ChatMessage> messages = new ArrayList<>();

    // 채팅방 참여자, ChatRoom은 participants 리스트를 통해 참여자 정보를 저장
    @OneToMany(mappedBy = "chatRoom",
                            cascade = CascadeType.ALL,
                            orphanRemoval = true,
                            fetch = FetchType.EAGER)
    private List<ChatParticipant> participants = new ArrayList<>();

    // ✅ 채팅방 개설자 (Member 테이블과 연관관계 추가)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Member owner;

    public ChatRoom(String name, Member owner) {
        super();
        this.name = name;
        this.owner = owner;

    }

    public void addParticipant(ChatParticipant participant) {
        participants.add(participant);
        participant.setChatRoom(this);
    }
}
