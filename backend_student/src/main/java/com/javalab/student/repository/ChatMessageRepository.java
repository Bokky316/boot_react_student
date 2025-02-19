package com.javalab.student.repository;

import com.javalab.student.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * ✅ 특정 채팅방의 모든 메시지 조회 (오래된 순)
     */
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);

    /**
     * ✅ 특정 사용자가 보낸 메시지 조회
     */
    List<ChatMessage> findBySenderIdOrderBySentAtDesc(Long senderId);

    /**
     * ✅ 특정 사용자에게 수신된 메시지 조회
     */
    //List<ChatMessage> findByReceiverIdOrderBySentAtDesc(Long receiverId);

    /**
     * ✅ 특정 사용자에게 읽지 않은 메시지 개수 조회
     */
    //@Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.receiver.id = :receiverId AND cm.read = false")
    //long countUnreadMessages(@Param("receiverId") Long receiverId);

    /**
     * ✅ 특정 사용자에게 읽지 않은 메시지 개수 조회
     */
    //@Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiver.id = :receiverId AND m.read = false")
    //long countUnreadMessages(@Param("receiverId") Long receiverId);

    // ✅ 읽지 않은 메시지 개수 조회 (수정된 메서드명)
    //long countByIsReadFalseAndReceiverId(Long receiverId);
    //long countByIsReadFalseAndReceiverId(Long receiverId);



}
