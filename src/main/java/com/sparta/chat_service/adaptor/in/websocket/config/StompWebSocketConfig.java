package com.sparta.chat_service.adaptor.in.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// STOMP over WebSocket 인바운드 어댑터 설정
@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 구독 prefix (서버 -> 클라이언트)
		registry.enableSimpleBroker("/topic", "/queue");
		// 발행 prefix (클라이언트 -> 서버)
		registry.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// STOMP 핸드셰이크 엔드포인트
		registry.addEndpoint("/ws-chat")
				.setAllowedOriginPatterns("*")
				.withSockJS();
	}
}
