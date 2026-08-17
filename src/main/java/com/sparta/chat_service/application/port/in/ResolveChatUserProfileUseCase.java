package com.sparta.chat_service.application.port.in;

import com.sparta.chat_service.domain.model.ChatUserProfile;

// 채팅용 회원 프로필 조회 (로컬 → 없으면 Member → 저장)
public interface ResolveChatUserProfileUseCase {

	ChatUserProfile resolve(String memberUuid);
}
