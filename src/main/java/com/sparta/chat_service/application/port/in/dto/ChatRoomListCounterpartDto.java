package com.sparta.chat_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

// 목록용 상대. 화면에는 안 그리고 UUID만 둔다
@Getter
@Builder
public class ChatRoomListCounterpartDto {

	private final String memberUuid;
}
