package com.sparta.chat_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

// 상세 상단 판매자 (물건을 올린 사람). 등급 없음
@Getter
@Builder
public class ChatRoomDetailSellerDto {

	private final String memberUuid;
	private final String nickname;
}
