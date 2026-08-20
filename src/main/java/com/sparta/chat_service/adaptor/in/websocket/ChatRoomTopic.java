package com.sparta.chat_service.adaptor.in.websocket;

import java.util.Optional;

// 방 안 말풍선 topic 목적지
public final class ChatRoomTopic {

	public static final String PREFIX = "/topic/chat.";

	private ChatRoomTopic() {
	}

	public static Optional<String> roomIdFrom(String destination) {
		if (destination == null || !destination.startsWith(PREFIX)) {
			return Optional.empty();
		}
		String roomId = destination.substring(PREFIX.length()).trim();
		if (roomId.isBlank()) {
			return Optional.empty();
		}
		return Optional.of(roomId);
	}
}
