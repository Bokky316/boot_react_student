package com.javalab.student.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javalab.student.dto.MessageRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MessagePublisherService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String CHANNEL_NAME = "chat_channel";

    /**
     * ✅ 생성자 주입 시 @Qualifier 적용 (redisStringTemplate 사용)
     */
    public MessagePublisherService(
            @Qualifier("redisStringTemplate") RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * ✅ 메시지를 Redis Pub/Sub으로 발행하는 메서드
     */
    public void publishMessage(MessageRequestDto requestDto) {
        log.info("📨 Redis 메시지 발행 요청 - senderId={}, receiverId={}, content={}",
                requestDto.getSenderId(), requestDto.getReceiverId(), requestDto.getContent());

        if (requestDto.getSenderId() == null || requestDto.getReceiverId() == null) {
            log.error("❌ 메시지 발행 실패: 발신자 또는 수신자 ID가 누락되었습니다.");
            throw new IllegalArgumentException("발신자 또는 수신자 ID가 누락되었습니다.");
        }

        try {
            // ✅ JSON 문자열로 변환 후 Redis Pub/Sub으로 발행
            String jsonMessage = objectMapper.writeValueAsString(requestDto.toMap()); // Map 형식의 Dto를 JSON 문자열로 변환
            redisTemplate.convertAndSend(CHANNEL_NAME, jsonMessage); // 채널명으로 메시지 발행(메시지는 JSON 문자열), 이렇게 발행하면 Redis Subscriber가 메시지를 수신할 수  있음

            log.info("📩 Redis 메시지 발행 완료! senderId={}, receiverId={}, content={}",
                    requestDto.getSenderId(), requestDto.getReceiverId(), requestDto.getContent());

        } catch (Exception e) {
            log.error("❌ 메시지 발행 중 오류 발생", e);
            throw new RuntimeException("메시지 발행 실패", e);
        }
    }
}
