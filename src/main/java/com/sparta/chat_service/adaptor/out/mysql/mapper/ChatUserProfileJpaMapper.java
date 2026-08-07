package com.sparta.chat_service.adaptor.out.mysql.mapper;

import com.sparta.chat_service.adaptor.out.mysql.entity.ChatUserProfileEntity;
import com.sparta.chat_service.domain.model.ChatUserProfile;
import org.springframework.stereotype.Component;

// 도메인 <-> JPA 엔티티 매핑
@Component
public class ChatUserProfileJpaMapper {

	public ChatUserProfileEntity toEntity(ChatUserProfile profile) {
		return ChatUserProfileEntity.builder()
				.memberUuid(profile.getMemberUuid())
				.nickname(profile.getNickname())
				.profileImageUrl(profile.getProfileImageUrl())
				.updatedAt(profile.getUpdatedAt())
				.build();
	}

	public ChatUserProfile toDomain(ChatUserProfileEntity entity) {
		return ChatUserProfile.restore(
				entity.getMemberUuid(),
				entity.getNickname(),
				entity.getProfileImageUrl(),
				entity.getUpdatedAt()
		);
	}
}
