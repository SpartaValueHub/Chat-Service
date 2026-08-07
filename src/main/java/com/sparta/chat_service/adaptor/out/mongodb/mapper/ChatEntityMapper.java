package com.sparta.chat_service.adaptor.out.mongodb.mapper;

import com.sparta.chat_service.adaptor.out.mongodb.entity.ChatMessageEntity;
import com.sparta.chat_service.adaptor.out.mongodb.entity.ChatRoomEntity;
import com.sparta.chat_service.application.port.dto.ChatMessageGetDto;
import com.sparta.chat_service.application.port.dto.ChatMessageSaveDto;
import com.sparta.chat_service.application.port.dto.ChatRoomGetDto;
import com.sparta.chat_service.application.port.dto.ChatRoomSaveDto;
import com.sparta.chat_service.domain.model.ChatRoomStatus;
import com.sparta.chat_service.domain.model.MessageType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Collections;

// 기존 Port DTO <-> Mongo 문서 브릿지 (STOMP 전환 전 임시)
@Component
public class ChatEntityMapper {

	public Flux<ChatMessageGetDto> chatMessageGetDtoFlux(Flux<ChatMessageEntity> chatMessageEntityFlux) {
		return chatMessageEntityFlux.map(this::toChatMessageGetDto);
	}

	public ChatMessageGetDto toChatMessageGetDto(ChatMessageEntity chatMessageEntity) {
		return ChatMessageGetDto.builder()
				.chatMessageUuid(chatMessageEntity.getId())
				.chatRoomUuid(chatMessageEntity.getRoomId())
				.message(chatMessageEntity.getContent())
				.messageType(chatMessageEntity.getMessageType())
				.senderUuid(chatMessageEntity.getSenderUuid())
				.createdAt(chatMessageEntity.getCreatedAt() != null ? chatMessageEntity.getCreatedAt().toString() : null)
				.updatedAt(null)
				.build();
	}

	public ChatMessageEntity toEntity(ChatMessageSaveDto chatMessageSaveDto) {
		Instant now = Instant.now();
		String messageType = chatMessageSaveDto.getMessageType() != null
				? chatMessageSaveDto.getMessageType()
				: MessageType.TEXT.name();

		return ChatMessageEntity.builder()
				.roomId(chatMessageSaveDto.getChatRoomUuid())
				.messageType(messageType)
				.content(chatMessageSaveDto.getMessage())
				.senderUuid(chatMessageSaveDto.getSenderUuid())
				.metadata(null)
				.createdAt(now)
				.build();
	}

	public ChatRoomEntity toChatRoomEntity(ChatRoomSaveDto chatRoomSaveDto) {
		Instant now = Instant.now();
		return ChatRoomEntity.builder()
				.listingUuid(chatRoomSaveDto.getRoomName())
				.participants(Collections.emptyList())
				.lastMessage(null)
				.status(ChatRoomStatus.ACTIVE.name())
				.createdAt(now)
				.updatedAt(now)
				.build();
	}

	public ChatRoomGetDto toChatRoomGetDto(ChatRoomEntity entity) {
		return toChatRoomGetDto(entity, null);
	}

	public ChatRoomGetDto toChatRoomGetDto(ChatRoomEntity entity, ChatMessageEntity lastMessage) {
		String lastMessageContent = null;
		String lastMessageAt = null;

		if (entity.getLastMessage() != null) {
			lastMessageContent = entity.getLastMessage().getContent();
			lastMessageAt = entity.getLastMessage().getCreatedAt() != null
					? entity.getLastMessage().getCreatedAt().toString()
					: null;
		} else if (lastMessage != null) {
			lastMessageContent = lastMessage.getContent();
			lastMessageAt = lastMessage.getCreatedAt() != null ? lastMessage.getCreatedAt().toString() : null;
		}

		return ChatRoomGetDto.builder()
				.chatRoomUuid(entity.getId())
				.roomName(entity.getListingUuid())
				.lastMessage(lastMessageContent)
				.lastMessageAt(lastMessageAt)
				.createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null)
				.updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null)
				.build();
	}
}
