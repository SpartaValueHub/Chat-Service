package com.sparta.chat_service.adaptor.out.mongodb;

import com.sparta.chat_service.adaptor.out.mongodb.entity.ChatRoomEntity;
import com.sparta.chat_service.adaptor.out.mongodb.entity.LastMessageDocument;
import com.sparta.chat_service.adaptor.out.mongodb.mapper.ChatMongoMapper;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.application.port.out.SaveChatRoomPort;
import com.sparta.chat_service.application.port.out.UpdateChatRoomLastMessagePort;
import com.sparta.chat_service.application.port.out.UpdateParticipantLastReadPort;
import com.sparta.chat_service.domain.model.ChatRoom;
import com.sparta.chat_service.domain.model.LastMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

// chat_rooms Mongo Adapter
@Repository
@RequiredArgsConstructor
public class ChatRoomMongoAdapter implements LoadChatRoomPort, SaveChatRoomPort, UpdateChatRoomLastMessagePort,
		UpdateParticipantLastReadPort {

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
	public Optional<ChatRoom> findById(String roomId) {
		if (roomId == null || roomId.isBlank()) {
			return Optional.empty();
		}
		ChatRoomEntity entity = reactiveMongoTemplate.findById(roomId, ChatRoomEntity.class)
				.block(MONGO_TIMEOUT);
		return Optional.ofNullable(entity).map(chatMongoMapper::toDomain);
	}

	@Override
	public List<ChatRoom> findByParticipant(String memberUuid) {
		Query query = Query.query(Criteria.where("participants.member_uuid").is(memberUuid));
		return findRooms(query);
	}

	@Override
	public List<ChatRoom> findByParticipantAndProductPost(String memberUuid, String productPostUuid) {
		Query query = Query.query(
				Criteria.where("product_post_uuid").is(productPostUuid)
						.and("participants.member_uuid").is(memberUuid)
		);
		return findRooms(query);
	}

	private List<ChatRoom> findRooms(Query query) {
		List<ChatRoomEntity> entities = reactiveMongoTemplate.find(query, ChatRoomEntity.class)
				.collectList()
				.block(MONGO_TIMEOUT);
		if (entities == null || entities.isEmpty()) {
			return List.of();
		}
		return entities.stream()
				.map(chatMongoMapper::toDomain)
				.toList();
	}

	@Override
	public ChatRoom save(ChatRoom chatRoom) {
		ChatRoomEntity saved = reactiveMongoTemplate.save(chatMongoMapper.toEntity(chatRoom))
				.block(MONGO_TIMEOUT);
		return chatMongoMapper.toDomain(saved);
	}

	@Override
	public void updateLastMessage(String roomId, LastMessage lastMessage) {
		if (roomId == null || roomId.isBlank() || lastMessage == null) {
			return;
		}
		Query query = Query.query(Criteria.where("_id").is(roomId));
		Update update = new Update()
				.set("last_message", LastMessageDocument.builder()
						.content(lastMessage.getContent())
						.createdAt(lastMessage.getCreatedAt())
						.build())
				.set("updated_at", lastMessage.getCreatedAt());
		reactiveMongoTemplate.updateFirst(query, update, ChatRoomEntity.class)
				.block(MONGO_TIMEOUT);
	}

	@Override
	public void updateLastRead(String roomId, String memberUuid, Instant lastReadAt) {
		if (roomId == null || roomId.isBlank() || memberUuid == null || memberUuid.isBlank() || lastReadAt == null) {
			return;
		}
		Query query = Query.query(Criteria.where("_id").is(roomId)
				.and("participants.member_uuid").is(memberUuid.trim()));
		Update update = new Update().set("participants.$.last_read_at", lastReadAt);
		reactiveMongoTemplate.updateFirst(query, update, ChatRoomEntity.class)
				.block(MONGO_TIMEOUT);
	}
}
