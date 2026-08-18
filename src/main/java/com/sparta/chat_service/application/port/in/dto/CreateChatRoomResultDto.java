package com.sparta.chat_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

// 채팅방 생성(또는 기존 방 재사용) 결과
@Getter
@Builder
public class CreateChatRoomResultDto {

	// 채팅방 ID
	private final String roomId;
	// 상품 게시글 UUID
	private final String productPostUuid;
	// 구매자 회원 UUID
	private final String buyerUuid;
	// 판매자 회원 UUID
	private final String sellerUuid;
	// 기존 방을 재사용했는지 여부
	private final boolean reused;
}
