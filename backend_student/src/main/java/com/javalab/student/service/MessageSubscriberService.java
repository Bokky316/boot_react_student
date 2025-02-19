package com.javalab.student.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javalab.student.dto.MessageRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSubscriberService implements org.springframework.data.redis.connection.MessageListener {

    private final SimpMessagingTemplate messagingTemplate; // ✅ WebSocket을 통해 클라이언트에게 메시지를 전송하는 역할
    private final ObjectMapper objectMapper;

    /**
     *  Redis 메시지 수신
     *  - Redis에서 메시지를 수신하는 역할.
     *  - redisMessage : Redis Publiser가 발행한 메시지
     *  - 이 역할을 수행한 후, 구독 중인 WebSocket 클라이언트에게 메시지를 전송하면 된다.
     *    메시지를 수신하고 WebSocket을 통해 클라이언트에게 보냅니다.
     */
    @Override
    public void onMessage(Message redisMessage, byte[] pattern) { // ✅ RedisMessage는 변수로 사용
        try {
            // 1. Redis 메시지 수신
            String jsonMessage = new String(redisMessage.getBody()); // Redis Publiser가 발행한 메시지 getBody()로 가져와서 String으로 변환
            log.info("🔹 Redis Subscriber 에서 수신한 경로 : {}, 메시지 내용: {}", new String(pattern), jsonMessage);

            // 2. 전달받은 메시지 내용을 MessageRequestDto로 변환
            MessageRequestDto messageDto = objectMapper.readValue(jsonMessage, MessageRequestDto.class);
            log.info("✅ WebSocket으로 메시지 전송: /topic/chat/{}", messageDto.getReceiverId());

            // 3. Redis Pub에서 보낸 메시지를 WebSocket을 통해 클라이언트에게 전달
            // WebSocket을 통해 클라이언트에게 메시지를 보내는 역할을 한다.
            // convertAndSend : 특정 주제에 메시지를 보내는 메서드, 여기서 주제는 "/topic/chat/{receiverId}"로 설정
            // messageDto.getReceiverId() : 수신자 ID
            // objectMapper.writeValueAsString(messageDto) : MessageRequestDto를 JSON 문자열로 변환한 형태의 데이터
            // 웹소켓 서버는 기본적으로 모든 메시지를 브로드 캐스트하기 때문에 특정 사용자에게만 메시지를 전달하려면 topic/chat/{receiverId}로 구독 경로를 설정해야 함
            messagingTemplate.convertAndSend("/topic/chat/" + messageDto.getReceiverId(), objectMapper.writeValueAsString(messageDto));

            // 4. 위 코드 이후의 순서
            // 1) Spring WebSocket 메시지 브로커(SimpleBroker)가 메시지를 전달하여,
            //    해당 경로(/topic/chat/{receiverId})를 구독 중인 클라이언트가 수신.
            // 2) Spring이 내부적으로 WebSocket을 통해 메시지를 클라이언트에게 비동기적으로 전달.
            // 3) 클라이언트가 받는 데이터는 MessageRequestDto 형식으로 전달되며, 이를 화면에 표시.
            /*
            {
                "senderId": 2,
                "receiverId": 1,
                "content": "안녕하세요!"
            }
            */


        } catch (Exception e) {
            log.error("❌ 메시지 처리 중 오류 발생", e);
        }
    }
}
