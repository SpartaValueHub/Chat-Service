package com.sparta.chat_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

// 상세 말풍선용 상대 (1:1에서 나 아닌 참여자)
@Getter
@Builder
public class ChatRoomDetailCounterpartDto {

	private final String memberUuid;
	private final String nickname;
	private final String profileImageUrl;
}
