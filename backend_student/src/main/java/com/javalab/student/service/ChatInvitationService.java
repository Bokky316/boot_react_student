package com.javalab.student.service;

import com.javalab.student.constant.ChatInvitationStatus;
import com.javalab.student.entity.ChatInvitation;
import com.javalab.student.entity.ChatParticipant;
import com.javalab.student.entity.ChatRoom;
import com.javalab.student.entity.Member;
import com.javalab.student.repository.ChatInvitationRepository;
import com.javalab.student.repository.ChatParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatInvitationService {

    private final ChatInvitationRepository chatInvitationRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    //private final ChatRoomRepository chatRoomRepository;
    //private final MemberRepository memberRepository;

    /**
     * 🔹 특정 회원이 초대받은 채팅방 목록 조회
     */
//    @Transactional(readOnly = true)
//    public List<ChatInvitation> getPendingInvitations(Long memberId) {
//        return chatInvitationRepository.findByInvitedMemberIdAndStatus(memberId, ChatInvitationStatus.PENDING);
//    }

    /**
     * 🔹 특정 회원이 초대받은 채팅방 개수 조회 (배지 표시용)
     */
    @Transactional(readOnly = true)
    public int getPendingInvitationCount(Long memberId) {
        return chatInvitationRepository.countByInvitedMemberIdAndStatus(memberId, ChatInvitationStatus.PENDING);
    }

    /**
     * 🔹 채팅 초대 수락
     * - 채팅방에 참가자로 등록
     * - 초대 상태를 ACCEPTED로 변경
     *
     */
    @Transactional
    public void acceptInvitation(Long invitationId) {
        ChatInvitation invitation = chatInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("초대 정보를 찾을 수 없습니다."));

        // 채팅방 존재 여부 확인
        ChatRoom chatRoom = invitation.getChatRoom();
        Member invitedMember = invitation.getInvitedMember();

        // 참가자로 등록
        ChatParticipant newParticipant = new ChatParticipant();
        newParticipant.setChatRoom(chatRoom);
        newParticipant.setMember(invitedMember);
        newParticipant.setJoinedAt(java.time.LocalDateTime.now());
        chatParticipantRepository.save(newParticipant);

        // 초대 상태를 ACCEPTED로 변경
        invitation.setStatus(ChatInvitationStatus.ACCEPTED);
        chatInvitationRepository.save(invitation);
    }

    /**
     * 🔹 채팅 초대 거절
     */
    @Transactional
    public void declineInvitation(Long invitationId) {
        ChatInvitation invitation = chatInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("초대 정보를 찾을 수 없습니다."));

        // 초대 상태를 DECLINED로 변경
        invitation.setStatus(ChatInvitationStatus.DECLINED);
        chatInvitationRepository.save(invitation);
    }
}
