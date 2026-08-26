package com.sparta.chat_service.adaptor.out.mysql;

import com.sparta.chat_service.adaptor.out.mysql.entity.ChatProductPostEntity;
import com.sparta.chat_service.adaptor.out.mysql.mapper.ChatProductPostJpaMapper;
import com.sparta.chat_service.adaptor.out.mysql.repository.ChatProductPostJpaRepository;
import com.sparta.chat_service.application.port.out.LoadChatProductPostPort;
import com.sparta.chat_service.application.port.out.SaveChatProductPostPort;
import com.sparta.chat_service.domain.model.ChatProductPost;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

// chat_product_posts JPA Adapter
@Repository
@RequiredArgsConstructor
public class ChatProductPostJpaAdapter implements LoadChatProductPostPort, SaveChatProductPostPort {

	// chat_product_posts 저장소
	private final ChatProductPostJpaRepository chatProductPostJpaRepository;
	// 도메인 <-> 엔티티 매퍼
	private final ChatProductPostJpaMapper chatProductPostJpaMapper;

	@Override
	public List<ChatProductPost> findAllByProductPostUuids(Collection<String> productPostUuids) {
		if (productPostUuids == null || productPostUuids.isEmpty()) {
			return List.of();
		}
		return chatProductPostJpaRepository.findAllById(productPostUuids).stream()
				.map(chatProductPostJpaMapper::toDomain)
				.toList();
	}

	@Override
	public Optional<ChatProductPost> findByProductPostUuid(String productPostUuid) {
		if (productPostUuid == null || productPostUuid.isBlank()) {
			return Optional.empty();
		}
		return chatProductPostJpaRepository.findById(productPostUuid)
				.map(chatProductPostJpaMapper::toDomain);
	}

	@Override
	public ChatProductPost save(ChatProductPost productPost) {
		return chatProductPostJpaRepository.findById(productPost.getProductPostUuid())
				.map(entity -> updateExisting(entity, productPost))
				.orElseGet(() -> insertNew(productPost));
	}

	private ChatProductPost updateExisting(ChatProductPostEntity entity, ChatProductPost productPost) {
		entity.update(
				productPost.getProductPostImageUrl(),
				productPost.getProductPostName(),
				productPost.getPrice(),
				productPost.getTradeStatus(),
				productPost.getProductPostStatus()
		);
		return chatProductPostJpaMapper.toDomain(entity);
	}

	private ChatProductPost insertNew(ChatProductPost productPost) {
		ChatProductPostEntity saved = chatProductPostJpaRepository.save(
				chatProductPostJpaMapper.toEntity(productPost)
		);
		return chatProductPostJpaMapper.toDomain(saved);
	}
}
