package com.javalab.student.config.websoket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;


/**
 * 📌 웹소켓(WebSocket) 설정 클래스
 * - STOMP 프로토콜을 사용하여 실시간 메시징을 처리할 수 있도록 설정함
 * - 클라이언트와 서버 간의 실시간 메시지 교환을 가능하게 함
 */
@Configuration
@EnableWebSocketMessageBroker   // WebSocket 메시지 브로커 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 🔹 STOMP 웹소켓 엔드포인트 등록
     * - 클라이언트가 WebSocket 연결을 요청할 경로를 설정함
     * - React 클라이언트에서 `ws://43.200.140.40:8080/ws`로 접속 가능
     * - SockJS를 사용하여 WebSocket을 지원하지 않는 환경에서도 연결 가능하도록 설정
     * @param registry STOMP 엔드포인트를 등록하는 객체
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // 클라이언트가 웹소켓에 연결할 수 있는 엔드포인트
                //.setAllowedOrigins("http://43.200.140.40", "http://43.200.140.40:3000") // 허용된 출처 설정
                .setAllowedOriginPatterns("*")  // 모든 Origin 허용
                .withSockJS() // WebSocket이 지원되지 않는 경우 SockJS 사용
                .setInterceptors(new HttpSessionHandshakeInterceptor());  // Handshake 인터셉터 추가, 웹소켓은 연결 설정을 해놓으면 자신이 알아서 서버와의 악수를 요청하고 연결을 맺는다.

    }

    /**
     * 🔹 메시지 브로커 설정
     * - 메시지 브로커는 클라이언트 간 메시지를 중계하는 역할을 수행함
     * - `/topic` → 여러 사용자가 구독하는 채널(예: 공지사항, 그룹 채팅)
     * - `/queue` → 특정 사용자에게 보내는 1:1 메시지
     * - `/app` → 클라이언트가 서버로 메시지를 보낼 때 사용하는 prefix
     *
     * ⚠️ 현재는 REST API 방식으로 메시지를 처리하고 Redis를 이용한 중계를 사용하므로,
     *   웹소켓 메시지 브로커 기능은 실질적으로 사용하지 않음
     * @param registry 메시지 브로커를 설정하는 객체
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue"); // 클라이언트에게 메시지를 전달할 경로 (현재 사용하지 않음)
        registry.setApplicationDestinationPrefixes("/app"); // 클라이언트가 메시지를 서버로 보낼 때 사용할 경로 (현재 사용하지 않음)
    }
}
