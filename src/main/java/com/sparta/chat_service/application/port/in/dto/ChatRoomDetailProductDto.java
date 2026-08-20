package com.sparta.chat_service.application.port.in.dto;

import com.sparta.chat_service.domain.model.TradeStatus;
import lombok.Builder;
import lombok.Getter;

// 상세 상단 상품 스냅샷
@Getter
@Builder
public class ChatRoomDetailProductDto {

	private final String productPostUuid;
	private final String productPostImageUrl;
	private final String productPostName;
	private final Long price;
	private final TradeStatus tradeStatus;
}
