package com.sparta.chat_service.application.mapper;

import com.sparta.chat_service.application.port.dto.ChatMessageGetDto;
import com.sparta.chat_service.application.port.dto.ChatMessageRequestDto;
import com.sparta.chat_service.application.port.dto.ChatMessageResponseDto;
import com.sparta.chat_service.application.port.dto.ChatMessageSaveDto;
import com.sparta.chat_service.application.port.dto.ChatRoomGetDto;
import com.sparta.chat_service.application.port.dto.ChatRoomResponseDto;
import com.sparta.chat_service.domain.model.ChatMessage;
import com.sparta.chat_service.domain.model.MessageType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Instant;

@Component
public class ChatServiceMapper {

	public Flux<ChatMessageResponseDto> toChatMessageResponseDto(Flux<ChatMessage> chatMessageFlux) {
		return chatMessageFlux.map(chatMessage ->
				ChatMessageResponseDto.builder()
						.chatMessageUuid(chatMessage.getId())
						.chatRoomUuid(chatMessage.getRoomId())
						.messageType(chatMessage.getMessageType() != null ? chatMessage.getMessageType().name() : null)
						.message(chatMessage.getContent())
						.senderUuid(chatMessage.getSenderUuid())
						.createdAt(chatMessage.getCreatedAt() != null ? chatMessage.getCreatedAt().toString() : null)
						.updatedAt(null)
						.build()
		);
	}

	public Flux<ChatMessage> toChatMessage(Flux<ChatMessageGetDto> chatMessageGetDtoFlux) {
		return chatMessageGetDtoFlux.map(chatMessageGetDto ->
				ChatMessage.restore(
						chatMessageGetDto.getChatMessageUuid(),
						chatMessageGetDto.getChatRoomUuid(),
						chatMessageGetDto.getSenderUuid(),
						parseMessageType(chatMessageGetDto.getMessageType()),
						chatMessageGetDto.getMessage(),
						null,
						parseInstant(chatMessageGetDto.getCreatedAt())
				)
		);
	}

	public ChatMessageSaveDto toChatMessageSaveDto(ChatMessage chatMessage) {
		return ChatMessageSaveDto.builder()
				.chatRoomUuid(chatMessage.getRoomId())
				.messageType(chatMessage.getMessageType() != null ? chatMessage.getMessageType().name() : null)
				.message(chatMessage.getContent())
				.senderUuid(chatMessage.getSenderUuid())
				.build();
	}

	public ChatMessage fromChatMessageRequestDto(ChatMessageRequestDto chatMessageRequestDto) {
		MessageType messageType = parseMessageType(chatMessageRequestDto.getMessageType());
		return ChatMessage.builder()
				.roomId(chatMessageRequestDto.getChatRoomUuid())
				.messageType(messageType)
				.content(chatMessageRequestDto.getMessage())
				.senderUuid(chatMessageRequestDto.getSenderUuid())
				.metadata(null)
				.createdAt(Instant.now())
				.build();
	}

	public Flux<ChatRoomResponseDto> toChatRoomResponseDtoFlux(Flux<ChatRoomGetDto> chatRoomGetDtoFlux) {
		return chatRoomGetDtoFlux.map(this::toChatRoomResponseDto);
	}

	public ChatRoomResponseDto toChatRoomResponseDto(ChatRoomGetDto chatRoomGetDto) {
		return ChatRoomResponseDto.builder()
				.chatRoomUuid(chatRoomGetDto.getChatRoomUuid())
				.roomName(chatRoomGetDto.getRoomName())
				.lastMessage(chatRoomGetDto.getLastMessage())
				.lastMessageAt(chatRoomGetDto.getLastMessageAt())
				.createdAt(chatRoomGetDto.getCreatedAt())
				.updatedAt(chatRoomGetDto.getUpdatedAt())
				.build();
	}

	private MessageType parseMessageType(String messageType) {
		if (messageType == null || messageType.isBlank()) {
			return MessageType.TEXT;
		}
		return MessageType.valueOf(messageType);
	}

	private Instant parseInstant(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return Instant.parse(value);
	}
}
