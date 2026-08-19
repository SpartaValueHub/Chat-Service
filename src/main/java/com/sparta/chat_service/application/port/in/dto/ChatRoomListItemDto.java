package com.sparta.chat_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// 채팅 목록 한 줄
@Getter
@Builder
public class ChatRoomListItemDto {

	private final String roomId;
	private final ChatRoomListProductDto productPost;
	private final ChatRoomListCounterpartDto counterpart;
	private final ChatRoomListLastMessageDto lastMessage;
	private final int unreadCount;
	private final Instant updatedAt;
}
