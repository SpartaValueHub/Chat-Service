package com.sparta.chat_service.adaptor.in.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

// SUBSCRIBE/UNSUBSCRIBE로 방 안 화면 여부를 추적
@Component
@RequiredArgsConstructor
public class StompRoomPresenceInterceptor implements ChannelInterceptor {

	private final StompChatRoomPresenceTracker stompChatRoomPresenceTracker;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null || accessor.getCommand() == null) {
			return message;
		}
		StompCommand command = accessor.getCommand();
		if (command == StompCommand.SUBSCRIBE) {
			stompChatRoomPresenceTracker.subscribe(
					accessor.getSessionId(),
					accessor.getUser(),
					accessor.getDestination(),
					accessor.getSubscriptionId()
			);
			return message;
		}
		if (command == StompCommand.UNSUBSCRIBE) {
			stompChatRoomPresenceTracker.unsubscribe(
					accessor.getSessionId(),
					accessor.getDestination(),
					accessor.getSubscriptionId()
			);
			return message;
		}
		if (command == StompCommand.DISCONNECT) {
			stompChatRoomPresenceTracker.disconnect(accessor.getSessionId());
		}
		return message;
	}
}
