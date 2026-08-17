package com.sparta.chat_service.application.port.out;

import com.sparta.chat_service.domain.model.ChatUserProfile;

import java.util.Optional;

// 채팅 회원 프로필 Read Model 조회
public interface LoadChatUserProfilePort {

	Optional<ChatUserProfile> findByMemberUuid(String memberUuid);
}
