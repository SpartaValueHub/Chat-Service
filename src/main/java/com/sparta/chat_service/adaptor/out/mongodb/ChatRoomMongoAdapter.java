package com.sparta.chat_service.adaptor.out.mongodb;

import com.sparta.chat_service.adaptor.out.mongodb.entity.ChatRoomEntity;
import com.sparta.chat_service.adaptor.out.mongodb.mapper.ChatMongoMapper;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.application.port.out.SaveChatRoomPort;
import com.sparta.chat_service.domain.model.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

// chat_rooms Mongo Adapter
@Repository
@RequiredArgsConstructor
public class ChatRoomMongoAdapter implements LoadChatRoomPort, SaveChatRoomPort {

	private static final Duration MONGO_TIMEOUT = Duration.ofSeconds(5);

	// Reactive Mongo 템플릿 (서블릿 UseCase에서 block)
	private final ReactiveMongoTemplate reactiveMongoTemplate;
	// 도메인 <-> 문서 매퍼
	private final ChatMongoMapper chatMongoMapper;

	@Override
	public Optional<ChatRoom> findByProductPostAndMembers(
			String productPostUuid,
			String memberUuid1,
			String memberUuid2
	) {
		Query query = Query.query(
				Criteria.where("product_post_uuid").is(productPostUuid)
						.and("participants").size(2)
						.and("participants.member_uuid").all(List.of(memberUuid1, memberUuid2))
		);
		ChatRoomEntity entity = reactiveMongoTemplate.findOne(query, ChatRoomEntity.class)
				.block(MONGO_TIMEOUT);
		return Optional.ofNullable(entity).map(chatMongoMapper::toDomain);
	}

	@Override
	public ChatRoom save(ChatRoom chatRoom) {
		ChatRoomEntity saved = reactiveMongoTemplate.save(chatMongoMapper.toEntity(chatRoom))
				.block(MONGO_TIMEOUT);
		return chatMongoMapper.toDomain(saved);
	}
}
