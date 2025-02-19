package com.javalab.student.service;

import com.javalab.student.constant.ChatInvitationStatus;
import com.javalab.student.dto.ChatRoomResponseDto;
import com.javalab.student.entity.ChatInvitation;
import com.javalab.student.entity.ChatParticipant;
import com.javalab.student.entity.ChatRoom;
import com.javalab.student.entity.Member;
import com.javalab.student.repository.ChatInvitationRepository;
import com.javalab.student.repository.ChatParticipantRepository;
import com.javalab.student.repository.ChatRoomRepository;
import com.javalab.student.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatInvitationRepository chatInvitationRepository;
    private final SimpMessagingTemplate messagingTemplate; // ✅ WebSocket 메시지 전송
    private final MemberRepository memberRepository;
    private final ChatParticipantRepository chatParticipantRepository;


//    public ChatRoom createChatRoom(ChatRoom chatRoom) {
//        return chatRoomRepository.save(chatRoom);
//    }

    /**
     * 회원이 참여한 채팅방 목록을 조회하는 메서드
     */
    public List<ChatRoom> getChatRoomsByMemberId(Long memberId) {
        return chatRoomRepository.findByMemberId(memberId);
    }

    /**
     * 🔹 사용자가 개설한 채팅방 + 초대받은 채팅방 조회 후 DTO 변환
     * - 채팅방 ID, 채팅방 이름, 채팅방 개설일자, 방 개설자 ID, 방 개설자 이름, 참여 상태를 반환한다.
     * - 프론트엔드에서 "채팅하기" 컴포넌트에서 보여질 채팅방 목록을 조회한다.
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDto> getChatRoomAndInvitedByMemberId(Long memberId) {
        List<Object[]> results = chatRoomRepository.findUserChatRooms(memberId);

        return results.stream()
                .map(row -> new ChatRoomResponseDto(
                        ((Number) row[0]).longValue(),   // 채팅방 ID
                        (String) row[1],                // 채팅방 이름
                        ((Timestamp) row[2]).toLocalDateTime(), // 🔹 Timestamp -> LocalDateTime 변환
                        ((Number) row[3]).longValue(),  // 방 개설자 ID
                        (String) row[4],                // 방 개설자 이름
                        (String) row[5]                 // 참여 상태 ("JOINED" or "PENDING")
                ))
                .collect(Collectors.toList());
    }

    /**
     * 🔹 채팅방 초대를 수락하고 JOINED 상태로 변경
     */
    @Transactional
    public void joinChatRoom(Long invitationId) {
        // 1️⃣ 초대 정보 조회
        ChatInvitation invitation = chatInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("초대 정보를 찾을 수 없습니다."));

        // 2️⃣ 초대 상태 확인 (초대가 ACCEPTED 상태인지 확인)
        if (!ChatInvitationStatus.ACCEPTED.equals(invitation.getStatus())) {
            throw new IllegalStateException("초대를 수락한 후에만 채팅방에 참여할 수 있습니다.");
        }

        // 3️⃣ 초대 상태를 JOINED로 변경 (PENDING → JOINED)
        invitation.setStatus(ChatInvitationStatus.JOINED);
        chatInvitationRepository.save(invitation);

        log.info("✅ 채팅방 입장 완료 - 초대 ID: {}, 채팅방 ID: {}", invitationId, invitation.getChatRoom().getId());

        // 4️⃣ WebSocket을 통해 채팅방 입장 알림 (Redux에서 배지 감소)
        messagingTemplate.convertAndSend("/topic/chat/joined/" + invitation.getChatRoom().getId(), "USER_JOINED");
    }

    @Transactional
    public ChatRoom createChatRoom(String name, Long ownerId, Long inviteeId) {
        Member owner = memberRepository.findById(ownerId).orElseThrow(() -> new RuntimeException("사용자 없음"));
        Member invitee = memberRepository.findById(inviteeId).orElseThrow(() -> new RuntimeException("사용자 없음"));

        ChatRoom chatRoom = new ChatRoom(name, owner);
        chatRoomRepository.save(chatRoom);

        chatParticipantRepository.save(new ChatParticipant(chatRoom, owner));

        ChatInvitation chatInvitation = new ChatInvitation(chatRoom, owner, invitee, "채팅방 초대");
        chatInvitationRepository.save(chatInvitation);

        return chatRoom;
    }



}