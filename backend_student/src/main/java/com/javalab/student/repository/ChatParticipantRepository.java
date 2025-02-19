package com.javalab.student.repository;

import com.javalab.student.entity.ChatParticipant;
import com.javalab.student.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    // 특정 채팅방의 모든 참여자 조회
    List<ChatParticipant> findByChatRoomId(Long chatRoomId);

    // 특정 회원이 참여한 모든 채팅방 조회
    List<ChatParticipant> findByMemberId(Long memberId);

    // 특정 회원이 특정 채팅방에 참여하고 있는지 확인
    boolean existsByChatRoomIdAndMemberId(Long chatRoomId, Long memberId);

    List<ChatParticipant> findByChatRoom(ChatRoom foundChatRoom);
}
