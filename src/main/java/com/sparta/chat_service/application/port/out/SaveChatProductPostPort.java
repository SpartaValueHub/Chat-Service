package com.sparta.chat_service.application.port.out;

import com.sparta.chat_service.domain.model.ChatProductPost;

// 채팅 상품 게시글 Read Model 저장 (upsert)
public interface SaveChatProductPostPort {

	ChatProductPost save(ChatProductPost productPost);
}
