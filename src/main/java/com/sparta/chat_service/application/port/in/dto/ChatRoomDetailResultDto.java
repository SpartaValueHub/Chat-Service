package com.sparta.chat_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

// 채팅방 상세
@Getter
@Builder
public class ChatRoomDetailResultDto {

	private final String roomId;
	private final ChatRoomDetailProductDto productPost;
	private final ChatRoomDetailSellerDto seller;
	private final ChatRoomDetailCounterpartDto counterpart;
}
