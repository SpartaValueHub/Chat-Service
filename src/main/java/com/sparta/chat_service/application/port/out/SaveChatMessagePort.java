package com.sparta.chat_service.application.port.out;

import com.sparta.chat_service.domain.model.ChatMessage;

// 채팅 메시지 저장
public interface SaveChatMessagePort {

	ChatMessage save(ChatMessage chatMessage);
}
