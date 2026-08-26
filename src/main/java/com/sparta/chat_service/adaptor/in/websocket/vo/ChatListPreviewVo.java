package com.sparta.chat_service.adaptor.in.websocket.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

// STOMP로 목록 한 줄을 패치하는 미리보기
@Getter
@Builder
public class ChatListPreviewVo {

	private final String roomId;
	private final LastMessage lastMessage;
	private final int unreadCount;
	private final OffsetDateTime updatedAt;

	@Getter
	@Builder
	public static class LastMessage {

		private final String content;
		private final OffsetDateTime createdAt;
	}
}
