package com.sparta.chat_service.adaptor.in.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

// 핸드셰이크에서 회원 UUID를 세션에 저장 (헤더 또는 쿼리)
@Component
public class MemberUuidHandshakeInterceptor implements HandshakeInterceptor {

	static final String MEMBER_UUID_ATTRIBUTE = "memberUuid";
	private static final String MEMBER_UUID_HEADER = "X-Member-Uuid";

	@Override
	public boolean beforeHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Map<String, Object> attributes
	) {
		String memberUuid = firstHeader(request);
		if (isBlank(memberUuid) && request instanceof ServletServerHttpRequest servletRequest) {
			memberUuid = servletRequest.getServletRequest().getParameter(MEMBER_UUID_HEADER);
		}
		if (!isBlank(memberUuid)) {
			attributes.put(MEMBER_UUID_ATTRIBUTE, memberUuid.trim());
		}
		return true;
	}

	@Override
	public void afterHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Exception exception
	) {
		// no-op
	}

	private String firstHeader(ServerHttpRequest request) {
		return request.getHeaders().getFirst(MEMBER_UUID_HEADER);
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
