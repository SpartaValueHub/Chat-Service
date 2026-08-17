package com.sparta.chat_service.application.port.out;

import com.sparta.chat_service.domain.model.ChatUserProfile;

// 채팅 회원 프로필 Read Model 저장 (upsert)
public interface SaveChatUserProfilePort {

	ChatUserProfile save(ChatUserProfile profile);
}
