package com.sparta.chat_service.adaptor.in.websocket;

import com.sparta.chat_service.domain.exception.ChatAuthMissingException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;

// CONNECT/SEND에서 회원 UUID를 Principal로 고정
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

	private static final String MEMBER_UUID_HEADER = "X-Member-Uuid";

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null) {
			return message;
		}
		StompCommand command = accessor.getCommand();
		if (command == StompCommand.CONNECT) {
			String memberUuid = resolveMemberUuid(accessor);
			if (isBlank(memberUuid)) {
				throw new ChatAuthMissingException();
			}
			accessor.setUser(new MemberPrincipal(memberUuid.trim()));
			Map<String, Object> attributes = accessor.getSessionAttributes();
			if (attributes != null) {
				attributes.put(MemberUuidHandshakeInterceptor.MEMBER_UUID_ATTRIBUTE, memberUuid.trim());
			}
		}
		return message;
	}

	private String resolveMemberUuid(StompHeaderAccessor accessor) {
		String header = accessor.getFirstNativeHeader(MEMBER_UUID_HEADER);
		if (!isBlank(header)) {
			return header;
		}
		Map<String, Object> attributes = accessor.getSessionAttributes();
		if (attributes == null) {
			return null;
		}
		Object attribute = attributes.get(MemberUuidHandshakeInterceptor.MEMBER_UUID_ATTRIBUTE);
		return attribute == null ? null : attribute.toString();
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
