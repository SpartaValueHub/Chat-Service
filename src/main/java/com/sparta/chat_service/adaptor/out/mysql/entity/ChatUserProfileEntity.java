package com.sparta.chat_service.adaptor.out.mysql.entity;

import com.sparta.chat_service.domain.model.MemberGrade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// chat_user_profiles 테이블 매핑
@Entity
@Table(name = "chat_user_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatUserProfileEntity {

	// 회원 UUID (PK)
	@Id
	@Column(name = "member_uuid", length = 36, nullable = false)
	private String memberUuid;

	// 닉네임 (UNIQUE)
	@Column(name = "nickname", length = 50, nullable = false, unique = true)
	private String nickname;

	// 프로필 이미지 URL
	@Column(name = "profile_image_url", length = 500)
	private String profileImageUrl;

	// 회원 등급 (판매자만 채워질 수 있음)
	@Enumerated(EnumType.STRING)
	@Column(name = "member_grade", length = 20)
	private MemberGrade memberGrade;

	// 프로필 수정 시각
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Builder
	private ChatUserProfileEntity(
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

	public static ChatUserProfileEntity create(String memberUuid, String nickname, String profileImageUrl) {
		return ChatUserProfileEntity.builder()
				.memberUuid(memberUuid)
				.nickname(nickname)
				.profileImageUrl(profileImageUrl)
				.updatedAt(LocalDateTime.now())
				.build();
	}

	// 닉네임·프로필 이미지·회원 등급 갱신
	public void update(String nickname, String profileImageUrl, MemberGrade memberGrade) {
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
		this.memberGrade = memberGrade;
		this.updatedAt = LocalDateTime.now();
	}
}
