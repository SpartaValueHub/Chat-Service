package com.sparta.chat_service.adaptor.out.mongodb.restRepository;

import com.sparta.chat_service.adaptor.out.mongodb.entity.ChatMessageEntity;
import com.sparta.chat_service.adaptor.out.mongodb.entity.ChatRoomEntity;
import com.sparta.chat_service.adaptor.out.mongodb.entity.LastMessageDocument;
import com.sparta.chat_service.adaptor.out.mongodb.mapper.ChatEntityMapper;
import com.sparta.chat_service.application.port.dto.ChatMessageSaveDto;
import com.sparta.chat_service.application.port.dto.ChatRoomGetDto;
import com.sparta.chat_service.application.port.dto.ChatRoomSaveDto;
import com.sparta.chat_service.application.port.out.ChatServiceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Comparator;

@Repository
@RequiredArgsConstructor
public class ChaRestRepositoryImpl implements ChatServiceRepositoryPort {

	private final ReactiveMongoTemplate reactiveMongoTemplate;
	private final ChatEntityMapper chatEntityMapper;

	private static final Comparator<ChatRoomGetDto> LATEST_MESSAGE_FIRST =
			Comparator.comparing(
					ChatRoomGetDto::getLastMessageAt,
					Comparator.nullsLast(Comparator.reverseOrder())
			);

	@Override
	public Mono<Void> sendChatMessage(ChatMessageSaveDto chatMessageSaveDto) {
		ChatMessageEntity messageEntity = chatEntityMapper.toEntity(chatMessageSaveDto);
		Instant createdAt = messageEntity.getCreatedAt();

		Update lastMessageUpdate = new Update()
				.set("last_message", LastMessageDocument.builder()
						.content(messageEntity.getContent())
						.createdAt(createdAt)
						.build())
				.set("updated_at", Instant.now());

		return reactiveMongoTemplate.save(messageEntity)
				.then(reactiveMongoTemplate.updateFirst(
						Query.query(Criteria.where("_id").is(chatMessageSaveDto.getChatRoomUuid())),
						lastMessageUpdate,
						ChatRoomEntity.class
				))
				.then();
	}

	@Override
	public Flux<ChatRoomGetDto> getChatRooms() {
		return reactiveMongoTemplate.findAll(ChatRoomEntity.class)
				.map(chatEntityMapper::toChatRoomGetDto)
				.sort(LATEST_MESSAGE_FIRST);
	}

	@Override
	public Mono<ChatRoomGetDto> getChatRoomByUuid(String chatRoomUuid) {
		return reactiveMongoTemplate.findById(chatRoomUuid, ChatRoomEntity.class)
				.map(chatEntityMapper::toChatRoomGetDto);
	}

	@Override
	public Mono<ChatRoomGetDto> saveChatRoom(ChatRoomSaveDto chatRoomSaveDto) {
		ChatRoomEntity entity = chatEntityMapper.toChatRoomEntity(chatRoomSaveDto);
		return reactiveMongoTemplate.save(entity)
				.map(chatEntityMapper::toChatRoomGetDto);
	}
}
