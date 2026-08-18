package com.sparta.chat_service.adaptor.out.mysql;

import com.sparta.chat_service.adaptor.out.mysql.entity.ChatProductPostEntity;
import com.sparta.chat_service.adaptor.out.mysql.mapper.ChatProductPostJpaMapper;
import com.sparta.chat_service.adaptor.out.mysql.repository.ChatProductPostJpaRepository;
import com.sparta.chat_service.application.port.out.SaveChatProductPostPort;
import com.sparta.chat_service.domain.model.ChatProductPost;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// chat_product_posts JPA Adapter
@Repository
@RequiredArgsConstructor
public class ChatProductPostJpaAdapter implements SaveChatProductPostPort {

	// chat_product_posts 저장소
	private final ChatProductPostJpaRepository chatProductPostJpaRepository;
	// 도메인 <-> 엔티티 매퍼
	private final ChatProductPostJpaMapper chatProductPostJpaMapper;

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
				productPost.getSaleStatus()
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
