package com.sparta.chat_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 내 채팅방 목록
@Getter
@Builder
public class ChatRoomListResultDto {

	private final List<ChatRoomListItemDto> rooms;
}
