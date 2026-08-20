package com.sparta.chat_service.adaptor.in.web.vo;

import com.sparta.chat_service.domain.model.TradeStatus;
import lombok.Builder;
import lombok.Getter;

// 채팅방 상세 응답
@Getter
@Builder
public class ChatRoomDetailResponseVo {

	private final String roomId;
	private final ProductPost productPost;
	private final Seller seller;
	private final Counterpart counterpart;

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
	public static class Seller {

		private final String memberUuid;
		private final String nickname;
	}

	@Getter
	@Builder
	public static class Counterpart {

		private final String memberUuid;
		private final String nickname;
		private final String profileImageUrl;
	}
}
