package com.sparta.chat_service.application.port.in;

import com.sparta.chat_service.application.port.in.dto.ChatMessageItemDto;
import com.sparta.chat_service.application.port.in.dto.SendChatMessageCommandDto;

// 채팅 메시지 저장 후 실시간 전달용 결과 반환
public interface SendChatMessageUseCase {

	ChatMessageItemDto send(SendChatMessageCommandDto command);
}
