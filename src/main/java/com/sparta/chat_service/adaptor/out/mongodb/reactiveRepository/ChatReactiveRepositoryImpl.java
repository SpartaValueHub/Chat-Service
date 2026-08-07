package com.sparta.chat_service.adaptor.out.mongodb.reactiveRepository;

import com.mongodb.client.model.changestream.OperationType;
import com.sparta.chat_service.adaptor.out.mongodb.entity.ChatMessageEntity;
import com.sparta.chat_service.adaptor.out.mongodb.mapper.ChatEntityMapper;
import com.sparta.chat_service.application.port.dto.ChatMessageGetDto;
import com.sparta.chat_service.application.port.out.ChatServiceReactiveRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Date;

@Repository
@RequiredArgsConstructor
public class ChatReactiveRepositoryImpl implements ChatServiceReactiveRepositoryPort {

	private static final String COLLECTION = "chat_messages";

	private final ChatReactiveMongoRepository chatReactiveMongoRepository;
	private final ReactiveMongoTemplate reactiveMongoTemplate;
	private final ChatEntityMapper chatEntityMapper;

	@Override
	public Flux<ChatMessageGetDto> getChatByChatRoomUuid(String chatRoomUuid) {
		return chatEntityMapper.chatMessageGetDtoFlux(chatReactiveMongoRepository.findByRoomId(chatRoomUuid));
	}

	@Override
	public Flux<ChatMessageGetDto> getLatestChatByChatRoomUuid(String chatRoomUuid) {
		ChangeStreamOptions options = ChangeStreamOptions.builder()
				.filter(Aggregation.newAggregation(
						Aggregation.match(Criteria.where("operationType").is(OperationType.INSERT.getValue())),
						Aggregation.match(Criteria.where("fullDocument.room_id").is(chatRoomUuid))
				)).build();
		return chatEntityMapper.chatMessageGetDtoFlux(
				reactiveMongoTemplate.changeStream(COLLECTION, options, Document.class)
						.map(ChangeStreamEvent::getBody)
						.map(this::toEntity)
		);
	}

	private ChatMessageEntity toEntity(Document document) {
		Object idValue = document.get("_id");
		String id = idValue instanceof ObjectId objectId ? objectId.toString() : String.valueOf(idValue);
		Date createdAt = document.getDate("created_at");

		return ChatMessageEntity.builder()
				.id(id)
				.roomId(document.getString("room_id"))
				.senderUuid(document.getString("sender_uuid"))
				.content(document.getString("content"))
				.messageType(document.getString("message_type"))
				.metadata(null)
				.createdAt(createdAt != null ? createdAt.toInstant() : Instant.now())
				.build();
	}
}
