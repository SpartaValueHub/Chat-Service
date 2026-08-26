package com.sparta.chat_service.application.port.out.dto;

import com.sparta.chat_service.domain.model.TradeStatus;
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
	private final ProductPost productPost;

	@Getter
	@Builder
	public static class LastMessage {

		private final String content;
		private final Instant createdAt;
	}

	@Getter
	@Builder
	public static class ProductPost {

		private final String productPostUuid;
		private final String productPostImageUrl;
		private final String productPostName;
		private final Long price;
		private final TradeStatus tradeStatus;
	}
}
