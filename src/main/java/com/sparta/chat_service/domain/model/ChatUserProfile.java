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
	// 회원 등급 (판매자만 채워질 수 있음)
	private final MemberGrade memberGrade;
	// 프로필 수정 시각
	private final LocalDateTime updatedAt;

	@Builder
	private ChatUserProfile(
			String memberUuid,
			String nickname,
			String profileImageUrl,
			MemberGrade memberGrade,
			LocalDateTime updatedAt
	) {
		this.memberUuid = memberUuid;
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
		this.memberGrade = memberGrade;
		this.updatedAt = updatedAt;
	}

	public static ChatUserProfile create(String memberUuid, String nickname, String profileImageUrl) {
		return create(memberUuid, nickname, profileImageUrl, null);
	}

	public static ChatUserProfile create(
			String memberUuid,
			String nickname,
			String profileImageUrl,
			MemberGrade memberGrade
	) {
		return ChatUserProfile.builder()
				.memberUuid(memberUuid)
				.nickname(nickname)
				.profileImageUrl(profileImageUrl)
				.memberGrade(memberGrade)
				.updatedAt(LocalDateTime.now())
				.build();
	}

	public static ChatUserProfile restore(
			String memberUuid,
			String nickname,
			String profileImageUrl,
			MemberGrade memberGrade,
			LocalDateTime updatedAt
	) {
		return ChatUserProfile.builder()
				.memberUuid(memberUuid)
				.nickname(nickname)
				.profileImageUrl(profileImageUrl)
				.memberGrade(memberGrade)
				.updatedAt(updatedAt)
				.build();
	}

	// 닉네임·프로필 이미지 갱신. 등급이 비면 기존 값을 유지한다
	public ChatUserProfile update(String nickname, String profileImageUrl) {
		return update(nickname, profileImageUrl, this.memberGrade);
	}

	// 닉네임·프로필 이미지·회원 등급 갱신
	public ChatUserProfile update(String nickname, String profileImageUrl, MemberGrade memberGrade) {
		return ChatUserProfile.builder()
				.memberUuid(this.memberUuid)
				.nickname(nickname)
				.profileImageUrl(profileImageUrl)
				.memberGrade(memberGrade != null ? memberGrade : this.memberGrade)
				.updatedAt(LocalDateTime.now())
				.build();
	}

	// 상품 게시글 상세에서 온 판매자 닉네임만 반영. 프로필 이미지·등급은 유지
	public ChatUserProfile applySellerSnapshot(String nickname) {
		return update(nickname, this.profileImageUrl);
	}
}
