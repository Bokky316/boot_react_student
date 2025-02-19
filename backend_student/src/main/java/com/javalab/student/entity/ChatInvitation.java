package com.javalab.student.entity;

import com.javalab.student.constant.ChatInvitationStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * ✅ 채팅방 초대 엔티티
 * - 초대받은 사용자가 초대를 수락하기 전까지는 채팅방 참가자가 아님
 * - 초대받은 사용자가 초대를 수락하면 채팅방 참가자로 등록됨
 * - 채팅하기 메뉴의 배지의 개수를 파악할 때 이 엔티티의 초대 상태를 확인(status = "PENDING")하여 배지 개수를 파악함
 */
@Entity
@Table(name = "chat_invitation")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatInvitation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 초대된 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_member_id", nullable = false)
    private Member invitedMember;

    // 초대한 사용자 (방장)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviting_member_id", nullable = false)
    private Member invitingMember;

    // 초대된 채팅방
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    // 초대 메시지 (선택 사항)
    @Column(length = 255)
    private String invitationMessage;

    // 초대 상태 ("PENDING", "ACCEPTED", "DECLINED")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatInvitationStatus status;

    public ChatInvitation(ChatRoom chatRoom, Member owner, Member invitee, String invitationMessage) {
        super();
        this.chatRoom = chatRoom;       // 초대된 채팅방
        this.invitingMember = owner;    // 채팅방 개설자
        this.invitedMember = invitee;   // 초대받은 사용자
        this.invitationMessage = invitationMessage; // 초대 메시지
        this.status = ChatInvitationStatus.PENDING; // 초대 상태


    }
}
