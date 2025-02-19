package com.javalab.student.repository;

import com.javalab.student.constant.ChatInvitationStatus;
import com.javalab.student.entity.ChatInvitation;
import com.javalab.student.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatInvitationRepository extends JpaRepository<ChatInvitation, Long> {

    /**
     * 🔹 초대받은 채팅방 목록 조회[Native Query]
     * - PENDING 상태만 조회
     */
    //@Query(value = "SELECT * FROM chat_invitation WHERE invited_member_id = :invitedMemberId AND status = :status", nativeQuery = true)
    //List<ChatInvitation> findByInvitedMemberIdAndStatus(
    //                    @Param("invitedMemberId") Long invitedMemberId,
    //                    @Param("status") ChatInvitationStatus status
    //);

    @Query(value = "SELECT cr.* FROM chat_invitation ci " +
            "JOIN chat_room cr ON ci.chat_room_id = cr.chat_room_id " +
            "WHERE ci.invited_member_id = :memberId AND ci.status = 'PENDING'",
            nativeQuery = true)
    List<ChatRoom> findChatRoomsByInvitedMemberId(@Param("memberId") Long memberId);

    /**
     * 초대받은 채팅방 개수 조회[JPQL 버전]
     * - PENDING 상태만 조회
     * - 배지 표시용
     */
    @Query("SELECT COUNT(ci) FROM ChatInvitation ci WHERE ci.invitedMember.id = :invitedMemberId AND ci.status = :status")
    int countByInvitedMemberIdAndStatus(@Param("invitedMemberId") Long invitedMemberId, @Param("status") ChatInvitationStatus status);

    /**
     * 특정 사용자의 특정 채팅방 초대 조회
     */
    @Query("SELECT ci FROM ChatInvitation ci WHERE ci.invitedMember.id = :invitedMemberId AND ci.chatRoom.id = :chatRoomId")
    ChatInvitation findByInvitedMemberIdAndChatRoomId(@Param("invitedMemberId") Long invitedMemberId, @Param("chatRoomId") Long chatRoomId);

    List<ChatInvitation> findByInvitedMemberId(Long id);
}
