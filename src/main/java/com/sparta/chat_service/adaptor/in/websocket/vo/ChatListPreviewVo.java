package com.sparta.chat_service.adaptor.in.websocket.vo;

import com.sparta.chat_service.domain.model.TradeStatus;
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
	private final ProductPost productPost;

	@Getter
	@Builder
	public static class LastMessage {

		private final String content;
		private final OffsetDateTime createdAt;
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
