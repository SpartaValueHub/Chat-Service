package com.sparta.chat_service.application.port.in.dto;

import com.sparta.chat_service.domain.model.ProductPostStatus;
import com.sparta.chat_service.domain.model.TradeStatus;
import lombok.Builder;
import lombok.Getter;

// 목록용 상품 게시글 스냅샷
@Getter
@Builder
public class ChatRoomListProductDto {

	private final String productPostUuid;
	private final String productPostImageUrl;
	private final String productPostName;
	private final Long price;
	private final TradeStatus tradeStatus;
	private final ProductPostStatus productPostStatus;
}
