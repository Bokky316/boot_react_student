package com.javalab.student.repository;

import com.javalab.student.constant.ChatInvitationStatus;
import com.javalab.student.dto.MemberFormDto;
import com.javalab.student.dto.MessageRequestDto;
import com.javalab.student.entity.*;
import com.javalab.student.service.MessagePublisherService;
import com.javalab.student.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Slf4j
class ChatRoomRepositoryTest {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessagePublisherService messagePublisherService;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    @Autowired
    private ChatInvitationRepository chatInvitationRepository;

    /**
     * 테스트용 Member 생성 메서드
     */
    private Member createTestMember(String name, String email) {
        MemberFormDto memberFormDto = MemberFormDto.builder()
                .name(name)
                .email(email)
                .password("1234")
                .address("서울시 강남구")
                .phone("010-1234-5678")
                .build();

        Member member = Member.createMember(memberFormDto, passwordEncoder);
        return memberRepository.save(member);
    }

    /**
     * 채팅방 생성 및 저장 테스트
     * 시나리오:
     * 1. 두 명의 테스트용 회원(sender, receiver) 생성
     * 2. 두 회원을 참여자로 하는 새로운 채팅방 생성
     * 3. 각 회원을 채팅방 참여자로 등록
     * 4. sender가 receiver에게 첫 메시지 전송
     * 5. 저장된 채팅방을 다시 조회하여 모든 정보가 정확히 저장되었는지 검증
     *
     * 검증 포인트:
     * - 채팅방이 정상적으로 생성되었는지 확인
     * - 두 명의 참여자가 정확히 등록되었는지 확인
     * - 메시지가 정상적으로 저장되었는지 확인
     * - 메시지의 발신자와 수신자 정보가 정확한지 확인
     */
    @Test
    @DisplayName("채팅방 생성 및 저장 테스트")
    //@Commit   // 처음에는 커밋없이 테스트하고 성공하면 커밋 주석해제해서 실제로 DB에 반영
    void createAndSaveChatRoomTest() {
        // Given
        Member sender = createTestMember("홍길동", "hong@test.com");   // 메시지를 보낼 사람
        Member receiver = createTestMember("김길동", "kim@test.com");  // 메시지를 받을 사람

        // 두 회원이 참여할 채팅방 생성
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(sender.getName() + ", " + receiver.getName() + "의 채팅방");

        // 참여자 2객체 생성 및 추가
        ChatParticipant senderParticipant = new ChatParticipant();
        senderParticipant.setMember(sender);
        senderParticipant.setJoinedAt(LocalDateTime.now());
        chatRoom.addParticipant(senderParticipant); // 채팅방에 참여자 추가(첫번째 참여자)

        ChatParticipant receiverParticipant = new ChatParticipant();
        receiverParticipant.setMember(receiver);
        receiverParticipant.setJoinedAt(LocalDateTime.now());
        chatRoom.addParticipant(receiverParticipant);   // 채팅방에 참여자 추가(두번째 참여자)

        // When 채팅방 저장
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        // 첫 메시지 전송, 보내는 사람은 홍길동, 받는 사람은 김길동
        ChatMessage firstMessage = new ChatMessage();
        firstMessage.setContent("안녕하세요!");
        firstMessage.setChatRoom(savedChatRoom);
        firstMessage.setSender(sender);
        firstMessage.setSentAt(LocalDateTime.now());
        savedChatRoom.getMessages().add(firstMessage);  // 채팅방에 메시지 추가

        chatRoomRepository.flush(); // 변경사항 DB 반영 즉, 채팅방에서 새로운 메시지 추가, 데이터베이스에 새로운 채팅 메시지 저장

        // Then 위에서 저장한 채팅방을 다시 조회하여 검증
        ChatRoom foundChatRoom = chatRoomRepository.findById(savedChatRoom.getId())
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        // 검증
        assertNotNull(foundChatRoom);   // 채팅방이 존재하는지 확인
        assertEquals(2, foundChatRoom.getParticipants().size());    // 참여자 수 확인
        assertEquals(1, foundChatRoom.getMessages().size());        // 메시지 수 확인
        assertEquals("안녕하세요!", foundChatRoom.getMessages().get(0).getContent());    // 메시지 내용 확인
        assertEquals("홍길동", foundChatRoom.getMessages().get(0).getSender().getName());  // 보낸 사람 확인
    }

    /**
     * 사용자별 채팅방 조회 테스트
     * 시나리오:
     * 1. 두 명의 테스트용 회원(sender, receiver)을 생성
     * 2. 두 회원이 참여하는 하나의 채팅방을 생성하고 저장
     * 3. 각 회원별로 참여하고 있는 채팅방을 조회
     * 4. 각 회원이 정확히 하나의 채팅방에 참여하고 있는지 검증
     *
     * 검증 포인트:
     * - 각 회원이 참여한 채팅방이 존재하는지 확인
     * - 각 회원별로 참여하고 있는 채팅방의 수가 1개인지 확인
     */
    @Test
    @DisplayName("사용자별 채팅방 조회 테스트")
    @Commit
    void findChatRoomsByMemberTest() {
        // Given: 테스트용 회원 2명 생성 (각각 sender와 receiver)
        Member sender = createTestMember("홍길동1", "hong1@test.com");   // 첫 번째 회원 생성
        Member receiver = createTestMember("김길동1", "kim1@test.com");   // 두 번째 회원 생성

        // 두 회원이 참여하는 채팅방 생성
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(sender.getName() + ", " + receiver.getName() + "의 채팅방");

        // sender를 첫 번째 참여자로 추가
        ChatParticipant senderParticipant = new ChatParticipant();
        senderParticipant.setMember(sender);
        senderParticipant.setJoinedAt(LocalDateTime.now());
        chatRoom.addParticipant(senderParticipant);

        // receiver를 두 번째 참여자로 추가
        ChatParticipant receiverParticipant = new ChatParticipant();
        receiverParticipant.setMember(receiver);
        receiverParticipant.setJoinedAt(LocalDateTime.now());
        chatRoom.addParticipant(receiverParticipant);

        // 채팅방을 데이터베이스에 저장
        chatRoomRepository.save(chatRoom);

        // When: 각 회원이 참여하고 있는 채팅방 목록 조회
        List<ChatRoom> senderChatRooms = chatRoomRepository.findByMemberId(sender.getId());
        List<ChatRoom> receiverChatRooms = chatRoomRepository.findByMemberId(receiver.getId());

        // Then: 조회 결과 검증, 테스트(검증) 실패시 어디서 문제가 있는지 파악하기 위해서 출력문 추가
        assertFalse(senderChatRooms.isEmpty(), "sender의 채팅방 목록이 비어있지 않아야 함");
        assertFalse(receiverChatRooms.isEmpty(), "receiver의 채팅방 목록이 비어있지 않아야 함");
        assertEquals(1, senderChatRooms.size(), "sender는 하나의 채팅방에만 참여하고 있어야 함");
        assertEquals(1, receiverChatRooms.size(), "receiver는 하나의 채팅방에만 참여하고 있어야 함");
    }


    /**
     * ✅ 특정 이름을 가진 회원 검색 (이름 기준)
     */
    private Member findMemberByName(String name) {
        return Optional.ofNullable(memberRepository.findByName(name))
                .orElseThrow(() -> new RuntimeException("해당 이름의 사용자가 없습니다: " + name));
    }

    /**
     * ✅ 같은 채팅방 참가자에게 메시지를 전송하는 테스트 (이름으로 회원 검색)
     */
    @Test
    @DisplayName("특정 회원 이름 검색 후 채팅방 생성 및 메시지 전송 테스트")
    @Commit
    void sendMessageToSelectedUserByNameTest() {
        // Given: 회원 2명 생성
        Member sender = createTestMember("홍길동3", "hong3@test.com");
        Member receiver = createTestMember("김길동3", "kim3@test.com");

        // ✅ 특정 회원 검색 (이름 기준)
        Member selectedMember = findMemberByName("김길동3");
        assertNotNull(selectedMember, "선택한 회원이 존재해야 함");
        assertEquals(receiver.getName(), selectedMember.getName(), "검색된 회원이 receiver와 동일해야 함");

        // ✅ 채팅방 생성
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(sender.getName() + " - " + selectedMember.getName() + " 채팅방");

        // ✅ 채팅 참가자 추가
        ChatParticipant senderParticipant = new ChatParticipant();
        senderParticipant.setMember(sender);
        senderParticipant.setJoinedAt(LocalDateTime.now());
        chatRoom.addParticipant(senderParticipant);

        ChatParticipant receiverParticipant = new ChatParticipant();
        receiverParticipant.setMember(selectedMember);
        receiverParticipant.setJoinedAt(LocalDateTime.now());
        chatRoom.addParticipant(receiverParticipant);

        // ✅ 채팅방 저장 (참가자 자동 영속화), 채팅 참가자도 함께 저장
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        // ✅ 메시지 전송
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent("안녕하세요! 선택한 회원에게 메시지를 보냅니다.");
        chatMessage.setChatRoom(savedChatRoom); // ✅ ChatRoom 설정
        chatMessage.setSender(sender);
        chatMessage.setSentAt(LocalDateTime.now());

        // ✅ 메시지 저장 (연관관계의 주인이므로 명시적으로 저장)
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // ✅ 메시지를 ChatRoom의 messages 리스트에 추가
        savedChatRoom.getMessages().add(savedMessage);

        // ✅ 변경 사항을 DB에 반영
        chatRoomRepository.flush(); // 영속성 컨텍스트 반영

        // ✅ 채팅방과 메시지 정상 저장 검증
        ChatRoom foundChatRoom = chatRoomRepository.findById(savedChatRoom.getId())
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        assertNotNull(foundChatRoom, "채팅방이 존재해야 함");
        assertEquals(2, foundChatRoom.getParticipants().size(), "채팅방에 2명의 참여자가 있어야 함");
        assertEquals("안녕하세요! 선택한 회원에게 메시지를 보냅니다.", savedMessage.getContent(), "메시지 내용이 일치해야 함");
        assertEquals(sender.getId(), savedMessage.getSender().getId(), "메시지 발신자가 sender여야 함");

        System.out.println("✅ 메시지 전송 테스트 성공!");
    }



    /**
     * ✅ 채팅방 개설 후 특정 사용자에게 초대 메시지를 전송하는 테스트 (Redis Pub/Sub + WebSocket 포함)
     */
    @Test
    @DisplayName("채팅방 생성 후 초대 메시지를 전송하고 초대 정보를 저장하는 테스트")
    @Commit // 주석처리후 테스트하고 성공시 주석해제 해서 DB에 반영
    void createChatRoomAndSendInvitationTest() {
        // Given: 회원 2명 생성 (방장 & 초대 대상)
        Member owner = createTestMember("홍길동1", "hong1@example.com");
        Member invitee = createTestMember("김길동1", "kim1@example.com");

        log.info("owner: {}", owner);
        log.info("invited: {}", owner.getId());

        // ✅ 1. 채팅방 생성
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(owner.getName() + "의 채팅방");
        chatRoom.setOwner(owner);  // ✅ 개설자 필드 추가 (기존 createdBy 사용 안 함)
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        // ✅ 2. 채팅방 개설자를 참가자로 자동 등록
        ChatParticipant ownerParticipant = new ChatParticipant();
        ownerParticipant.setChatRoom(savedChatRoom);
        ownerParticipant.setMember(owner);
        ownerParticipant.setJoinedAt(LocalDateTime.now());
        chatParticipantRepository.save(ownerParticipant);

        // ✅ 3. 초대 메시지 DTO 생성
        MessageRequestDto invitationMessageDto = MessageRequestDto.builder()
                .senderId(owner.getId())
                .receiverId(invitee.getId())
                .content(invitee.getName() + "님을 채팅방에 초대합니다.")
                .build();

        // ✅ 4. 초대 메시지를 DB에 저장
        Message savedMessage = messageService.saveMessage(invitationMessageDto);

        // ✅ 5. ChatInvitation 생성 (초대 정보를 따로 저장)
        ChatInvitation chatInvitation = ChatInvitation.builder()
                .invitedMember(invitee)
                .invitingMember(owner)
                .chatRoom(savedChatRoom)
                .invitationMessage(savedMessage.getContent())
                .status(ChatInvitationStatus.PENDING)
                .build();

        chatInvitationRepository.save(chatInvitation);

        // ✅ 6. 메시지를 Redis Pub/Sub으로 발행
        messagePublisherService.publishMessage(invitationMessageDto);

        // ✅ 7. 초대 메시지가 DB에 존재하는지 확인
        List<Message> receivedMessages = messageRepository.findByReceiverOrderByRegTimeDesc(invitee);
        assertEquals(1, receivedMessages.size());

        // ✅ 8. 초대 메시지 내용 검증
        Message latestMessage = receivedMessages.get(0);
        assertEquals(invitationMessageDto.getContent(), latestMessage.getContent());
        assertEquals(owner.getId(), latestMessage.getSender().getId());
        assertEquals(invitee.getId(), latestMessage.getReceiver().getId());
        assertFalse(latestMessage.isRead());

        // ✅ 9. 초대 정보가 DB에 존재하는지 확인
        List<ChatInvitation> invitations = chatInvitationRepository.findByInvitedMemberId(invitee.getId());
        assertEquals(1, invitations.size());
        assertEquals(savedChatRoom.getId(), invitations.get(0).getChatRoom().getId());

        // ✅ 10. 채팅방이 정상적으로 저장되었는지 확인
        ChatRoom foundChatRoom = chatRoomRepository.findById(savedChatRoom.getId())
                .orElseThrow(() -> new RuntimeException("채팅방이 존재해야 함"));
        assertNotNull(foundChatRoom);

        // ✅ 11. 채팅방 개설자가 참가자로 등록되었는지 확인
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom(foundChatRoom);
        assertEquals(1, participants.size());
        assertEquals(owner.getId(), participants.get(0).getMember().getId());

        System.out.println("✅ 채팅방 생성 + 참가자 등록 + 초대 메시지 전송 + 초대 정보 저장 테스트 성공!");
    }









    /**
     * ✅ 읽지 않은 메시지 개수 조회 테스트
     */
//    @Test
//    @DisplayName("특정 사용자에게 읽지 않은 메시지 개수 조회 테스트")
//    void countUnreadMessagesTest() {
//        // Given: 테스트용 회원 2명 생성 (보낸 사람 & 받는 사람)
//        Member sender = createTestMember("홍길동", "hong@test.com");
//        Member receiver = createTestMember("김길동", "kim@test.com");
//
//        // ✅ 채팅방 생성
//        ChatRoom chatRoom = new ChatRoom();
//        chatRoom.setName(sender.getName() + " - " + receiver.getName() + " 채팅방");
//        chatRoom = chatRoomRepository.save(chatRoom);
//
//        // ✅ 읽지 않은 메시지 3개 생성 및 저장
//        for (int i = 1; i <= 3; i++) {
//            ChatMessage message = new ChatMessage();
//            message.setContent("안녕하세요! " + i);
//            message.setChatRoom(chatRoom);
//            message.setSender(sender);
//            message.setReceiver(receiver);
//            message.setSentAt(LocalDateTime.now());
//            message.setRead(false); // ✅ 읽지 않은 메시지 설정
//            chatMessageRepository.save(message);
//        }
//
//        // ✅ 읽지 않은 메시지 개수 확인
//        long unreadCount = chatMessageRepository.countByIsReadFalseAndReceiverId(receiver.getId());
//
//        // ✅ 검증: 예상 값 (3)과 실제 값 비교
//        assertEquals(3, unreadCount, "읽지 않은 메시지는 3개여야 함");
//    }






}