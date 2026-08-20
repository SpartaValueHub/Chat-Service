package com.sparta.chat_service.adaptor.out.mongodb;

import com.sparta.chat_service.adaptor.out.mongodb.entity.ChatMessageEntity;
import com.sparta.chat_service.adaptor.out.mongodb.mapper.ChatMongoMapper;
import com.sparta.chat_service.application.port.out.LoadChatMessagePort;
import com.sparta.chat_service.application.port.out.SaveChatMessagePort;
import com.sparta.chat_service.domain.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// chat_messages Mongo Adapter
@Repository
@RequiredArgsConstructor
public class ChatMessageMongoAdapter implements LoadChatMessagePort, SaveChatMessagePort {

	private static final Duration MONGO_TIMEOUT = Duration.ofSeconds(5);

	private final ReactiveMongoTemplate reactiveMongoTemplate;
	private final ChatMongoMapper chatMongoMapper;

	@Override
	public Optional<ChatMessage> findById(String messageId) {
		if (messageId == null || messageId.isBlank()) {
			return Optional.empty();
		}
		ChatMessageEntity entity = reactiveMongoTemplate.findById(messageId, ChatMessageEntity.class)
				.block(MONGO_TIMEOUT);
		return Optional.ofNullable(entity).map(chatMongoMapper::toDomain);
	}

	@Override
	public List<ChatMessage> findLatestByRoomId(String roomId, int limit) {
		Query query = Query.query(Criteria.where("room_id").is(roomId))
				.with(newestFirst())
				.limit(limit);
		return reverseToOldestFirst(find(query));
	}

	@Override
	public List<ChatMessage> findByRoomIdBefore(String roomId, ChatMessage cursor, int limit) {
		Criteria olderThanCursor = new Criteria().orOperator(
				Criteria.where("created_at").lt(cursor.getCreatedAt()),
				Criteria.where("created_at").is(cursor.getCreatedAt())
						.and("_id").lt(cursor.getId())
		);
		Query query = Query.query(Criteria.where("room_id").is(roomId).andOperator(olderThanCursor))
				.with(newestFirst())
				.limit(limit);
		return reverseToOldestFirst(find(query));
	}

	@Override
	public int countUnread(String roomId, String viewerUuid, Instant lastReadAt) {
		if (roomId == null || roomId.isBlank() || viewerUuid == null || viewerUuid.isBlank()) {
			return 0;
		}
		Criteria criteria = Criteria.where("room_id").is(roomId)
				.and("sender_uuid").ne(viewerUuid.trim());
		if (lastReadAt != null) {
			criteria = criteria.and("created_at").gt(lastReadAt);
		}
		Long count = reactiveMongoTemplate.count(Query.query(criteria), ChatMessageEntity.class)
				.block(MONGO_TIMEOUT);
		return count == null ? 0 : count.intValue();
	}

	@Override
	public ChatMessage save(ChatMessage chatMessage) {
		ChatMessageEntity saved = reactiveMongoTemplate.save(chatMongoMapper.toEntity(chatMessage))
				.block(MONGO_TIMEOUT);
		return chatMongoMapper.toDomain(saved);
	}

	private List<ChatMessageEntity> find(Query query) {
		List<ChatMessageEntity> entities = reactiveMongoTemplate.find(query, ChatMessageEntity.class)
				.collectList()
				.block(MONGO_TIMEOUT);
		if (entities == null || entities.isEmpty()) {
			return List.of();
		}
		return entities;
	}

	private Sort newestFirst() {
		return Sort.by(Sort.Direction.DESC, "created_at").and(Sort.by(Sort.Direction.DESC, "_id"));
	}

	private List<ChatMessage> reverseToOldestFirst(List<ChatMessageEntity> entities) {
		List<ChatMessage> messages = new ArrayList<>(entities.stream()
				.map(chatMongoMapper::toDomain)
				.toList());
		Collections.reverse(messages);
		return messages;
	}
}
