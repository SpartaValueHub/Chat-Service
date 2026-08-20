package com.sparta.chat_service.application.port.out.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// 채팅 목록 한 줄 실시간 패치
@Getter
@Builder
public class ChatListPreviewDto {

	private final String roomId;
	private final LastMessage lastMessage;
	private final int unreadCount;
	private final Instant updatedAt;

	@Getter
	@Builder
	public static class LastMessage {

		private final String content;
		private final Instant createdAt;
	}
}
