package com.sparta.chat_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 채팅방 메시지 이력
@Getter
@Builder
public class ChatMessageListResultDto {

	private final List<ChatMessageItemDto> messages;
}
