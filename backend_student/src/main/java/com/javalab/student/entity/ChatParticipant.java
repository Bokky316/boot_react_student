package com.javalab.student.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 채팅방 참가자 엔티티
 */
@Entity
@Table(name = "chat_participant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatParticipant extends BaseEntity  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 채팅방 참가자와 채팅방은 다대일 관계이다.
     * - 채팅방 참가자는 여러 채팅방에 참여할 수 있다.
     * - 이 연관관계의 주인은 채팅 참가자이다. 왜냐하면 채팅방 참가자가 채팅방을 참조하고 있기 때문이다.
     * - fetch type을 lazy로 하여 채팅방 참가자를 조회할 때 연관된 채팅방의 모든 속성을 즉시 로딩하지 않는다.
     *   만약 즉시 로딩하게 되면 채팅방 참가자를 조회할 때 연관된 채팅방의 모든 속성을 조회하게 되어 부하가 발생한다.
     *   채팅방 참가자를 조회할 때는 채팅방의 chat_room_id 정도만 조회하면 되기 때문에 lazy로 설정한다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;


    public ChatParticipant(ChatRoom chatRoom, Member owner) {
        super();
        this.chatRoom = chatRoom;
        this.member = owner;
        this.joinedAt = LocalDateTime.now();

    }
}