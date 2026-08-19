package com.sparta.chat_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// 채팅방 문서의 last_message 스냅샷
@Getter
@Builder
public class ChatRoomListLastMessageDto {

	private final String content;
	private final Instant createdAt;
}
