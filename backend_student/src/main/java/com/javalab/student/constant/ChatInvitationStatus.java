package com.javalab.student.constant;

/**
 * 채팅 초대 상태를 나타내는 열거형 상수
 * - 이 enum이 사용되는 곳은 ChatInvitation 엔티티이다.
 */
public enum ChatInvitationStatus {
    PENDING,  // 초대된 상태
    ACCEPTED, // 초대 수락
    JOINED,    // ✅ 초대가 승인되어 채팅에 참여한 상태
    DECLINED   // 초대 거절
}
