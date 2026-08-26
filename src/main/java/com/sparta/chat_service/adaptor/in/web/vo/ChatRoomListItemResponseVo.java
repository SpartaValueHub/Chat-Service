package com.sparta.chat_service.adaptor.in.web.vo;

import com.sparta.chat_service.domain.model.TradeStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

// 채팅 목록 한 줄
@Getter
@Builder
public class ChatRoomListItemResponseVo {

	private final String roomId;
	private final ProductPost productPost;
	private final Counterpart counterpart;
	private final LastMessage lastMessage;
	private final int unreadCount;
	private final OffsetDateTime updatedAt;

	@Getter
	@Builder
	public static class ProductPost {

		private final String productPostUuid;
		private final String productPostImageUrl;
		private final String productPostName;
		private final Long price;
		private final TradeStatus tradeStatus;
	}

	@Getter
	@Builder
	public static class Counterpart {

		private final String memberUuid;
	}

	@Getter
	@Builder
	public static class LastMessage {

		private final String content;
		private final OffsetDateTime createdAt;
	}
}
