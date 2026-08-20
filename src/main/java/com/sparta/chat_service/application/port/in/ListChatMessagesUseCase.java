package com.sparta.chat_service.application.port.in;

import com.sparta.chat_service.application.port.in.dto.ChatMessageListResultDto;

// 채팅방 메시지 이력 조회
public interface ListChatMessagesUseCase {

	ChatMessageListResultDto list(String memberUuid, String roomId, String beforeMessageId, Integer limit);
}
