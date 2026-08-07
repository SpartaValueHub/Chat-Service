package com.sparta.chat_service.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 채팅용 회원 프로필 (MySQL)
@Getter
public class ChatUserProfile {

	// 회원 UUID
	private final String memberUuid;
	// 닉네임
	private final String nickname;
	// 프로필 이미지 URL
	private final String profileImageUrl;
	// 프로필 수정 시각
	private final LocalDateTime updatedAt;

	@Builder
	private ChatUserProfile(
			String memberUuid,
			String nickname,
			String profileImageUrl,
			LocalDateTime updatedAt
	) {
		this.memberUuid = memberUuid;
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
		this.updatedAt = updatedAt;
	}

	public static ChatUserProfile create(String memberUuid, String nickname, String profileImageUrl) {
		return ChatUserProfile.builder()
				.memberUuid(memberUuid)
				.nickname(nickname)
				.profileImageUrl(profileImageUrl)
				.updatedAt(LocalDateTime.now())
				.build();
	}

	public static ChatUserProfile restore(
			String memberUuid,
			String nickname,
			String profileImageUrl,
			LocalDateTime updatedAt
	) {
		return ChatUserProfile.builder()
				.memberUuid(memberUuid)
				.nickname(nickname)
				.profileImageUrl(profileImageUrl)
				.updatedAt(updatedAt)
				.build();
	}

	// 닉네임·프로필 이미지 갱신
	public ChatUserProfile update(String nickname, String profileImageUrl) {
		return ChatUserProfile.builder()
				.memberUuid(this.memberUuid)
				.nickname(nickname)
				.profileImageUrl(profileImageUrl)
				.updatedAt(LocalDateTime.now())
				.build();
	}
}
