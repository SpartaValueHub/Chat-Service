package com.sparta.chat_service.application.port.out;

import com.sparta.chat_service.domain.model.ChatUserProfile;

import java.util.Optional;

// Member 서비스에서 공개 프로필 조회
public interface LoadMemberProfilePort {

	Optional<ChatUserProfile> findByMemberUuid(String memberUuid);
}
