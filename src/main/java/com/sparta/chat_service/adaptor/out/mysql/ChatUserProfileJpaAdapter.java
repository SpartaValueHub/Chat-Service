package com.sparta.chat_service.adaptor.out.mysql;

import com.sparta.chat_service.adaptor.out.mysql.entity.ChatUserProfileEntity;
import com.sparta.chat_service.adaptor.out.mysql.mapper.ChatUserProfileJpaMapper;
import com.sparta.chat_service.adaptor.out.mysql.repository.ChatUserProfileJpaRepository;
import com.sparta.chat_service.application.port.out.LoadChatUserProfilePort;
import com.sparta.chat_service.application.port.out.SaveChatUserProfilePort;
import com.sparta.chat_service.domain.model.ChatUserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// chat_user_profiles JPA Adapter
@Repository
@RequiredArgsConstructor
public class ChatUserProfileJpaAdapter implements LoadChatUserProfilePort, SaveChatUserProfilePort {

	// chat_user_profiles 저장소
	private final ChatUserProfileJpaRepository chatUserProfileJpaRepository;
	// 도메인 <-> 엔티티 매퍼
	private final ChatUserProfileJpaMapper chatUserProfileJpaMapper;

	@Override
	public Optional<ChatUserProfile> findByMemberUuid(String memberUuid) {
		return chatUserProfileJpaRepository.findById(memberUuid)
				.map(chatUserProfileJpaMapper::toDomain);
	}

	@Override
	public ChatUserProfile save(ChatUserProfile profile) {
		return chatUserProfileJpaRepository.findById(profile.getMemberUuid())
				.map(entity -> updateExisting(entity, profile))
				.orElseGet(() -> insertNew(profile));
	}

	private ChatUserProfile updateExisting(ChatUserProfileEntity entity, ChatUserProfile profile) {
		entity.update(profile.getNickname(), profile.getProfileImageUrl());
		return chatUserProfileJpaMapper.toDomain(entity);
	}

	private ChatUserProfile insertNew(ChatUserProfile profile) {
		ChatUserProfileEntity saved = chatUserProfileJpaRepository.save(
				chatUserProfileJpaMapper.toEntity(profile)
		);
		return chatUserProfileJpaMapper.toDomain(saved);
	}
}
