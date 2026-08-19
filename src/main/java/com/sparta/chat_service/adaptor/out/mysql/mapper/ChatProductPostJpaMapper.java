package com.sparta.chat_service.adaptor.out.mysql.mapper;

import com.sparta.chat_service.adaptor.out.mysql.entity.ChatProductPostEntity;
import com.sparta.chat_service.domain.model.ChatProductPost;
import org.springframework.stereotype.Component;

// 도메인 <-> JPA 엔티티 매핑
@Component
public class ChatProductPostJpaMapper {

	public ChatProductPostEntity toEntity(ChatProductPost productPost) {
		return ChatProductPostEntity.builder()
				.productPostUuid(productPost.getProductPostUuid())
				.productPostImageUrl(productPost.getProductPostImageUrl())
				.productPostName(productPost.getProductPostName())
				.price(productPost.getPrice())
				.tradeStatus(productPost.getTradeStatus())
				.updatedAt(productPost.getUpdatedAt())
				.build();
	}

	public ChatProductPost toDomain(ChatProductPostEntity entity) {
		return ChatProductPost.restore(
				entity.getProductPostUuid(),
				entity.getProductPostImageUrl(),
				entity.getProductPostName(),
				entity.getPrice(),
				entity.getTradeStatus(),
				entity.getUpdatedAt()
		);
	}
}
