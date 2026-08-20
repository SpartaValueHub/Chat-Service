package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.SendChatMessageUseCase;
import com.sparta.chat_service.application.port.in.dto.ChatMessageItemDto;
import com.sparta.chat_service.application.port.in.dto.ChatMessageMetadataDto;
import com.sparta.chat_service.application.port.in.dto.SendChatMessageCommandDto;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.application.port.out.SaveChatMessagePort;
import com.sparta.chat_service.application.port.out.UpdateChatRoomLastMessagePort;
import com.sparta.chat_service.domain.exception.ChatAuthMissingException;
import com.sparta.chat_service.domain.exception.ChatRoomAccessDeniedException;
import com.sparta.chat_service.domain.exception.ChatRoomNotFoundException;
import com.sparta.chat_service.domain.exception.InvalidChatRoomRequestException;
import com.sparta.chat_service.domain.model.ChatMessage;
import com.sparta.chat_service.domain.model.ChatRoom;
import com.sparta.chat_service.domain.model.LastMessage;
import com.sparta.chat_service.domain.model.MessageMetadata;
import com.sparta.chat_service.domain.model.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendChatMessageService implements SendChatMessageUseCase {

	private static final String IMAGE_PREVIEW = "사진";

	private final LoadChatRoomPort loadChatRoomPort;
	private final SaveChatMessagePort saveChatMessagePort;
	private final UpdateChatRoomLastMessagePort updateChatRoomLastMessagePort;

	@Override
	public ChatMessageItemDto send(SendChatMessageCommandDto command) {
		if (command == null) {
			throw new InvalidChatRoomRequestException("요청 본문이 필요합니다.");
		}
		String senderUuid = requireMemberUuid(command.getSenderUuid());
		String roomId = requireText(command.getRoomId(), "roomId는 필수입니다.");
		ChatRoom room = requireAccessibleRoom(senderUuid, roomId);
		ChatMessage saved = saveChatMessagePort.save(toNewMessage(room.getId(), senderUuid, command));
		updateChatRoomLastMessagePort.updateLastMessage(
				room.getId(),
				LastMessage.create(preview(saved), saved.getCreatedAt())
		);
		return toItem(saved);
	}

	private ChatMessage toNewMessage(String roomId, String senderUuid, SendChatMessageCommandDto command) {
		MessageType messageType = command.getMessageType() == null ? MessageType.TEXT : command.getMessageType();
		String content = requireText(command.getContent(), "content는 필수입니다.");
		if (messageType == MessageType.TEXT) {
			return ChatMessage.createText(roomId, senderUuid, content);
		}
		if (messageType == MessageType.IMAGE) {
			return ChatMessage.createImage(roomId, senderUuid, content, toImageMetadata(command.getMetadata()));
		}
		throw new InvalidChatRoomRequestException("지원하지 않는 messageType입니다.");
	}

	private MessageMetadata toImageMetadata(ChatMessageMetadataDto metadataDto) {
		if (metadataDto == null) {
			return null;
		}
		return MessageMetadata.ofImage(
				metadataDto.getFileSize(),
				metadataDto.getImageWidth(),
				metadataDto.getImageHeight()
		);
	}

	private String preview(ChatMessage message) {
		if (message.getMessageType() == MessageType.IMAGE) {
			return IMAGE_PREVIEW;
		}
		return message.getContent();
	}

	private ChatMessageItemDto toItem(ChatMessage message) {
		return ChatMessageItemDto.builder()
				.messageId(message.getId())
				.senderUuid(message.getSenderUuid())
				.messageType(message.getMessageType())
				.content(message.getContent())
				.metadata(toMetadata(message.getMetadata()))
				.createdAt(message.getCreatedAt())
				.build();
	}

	private ChatMessageMetadataDto toMetadata(MessageMetadata metadata) {
		if (metadata == null) {
			return null;
		}
		return ChatMessageMetadataDto.builder()
				.fileSize(metadata.getFileSize())
				.imageWidth(metadata.getImageWidth())
				.imageHeight(metadata.getImageHeight())
				.reservationId(metadata.getReservationId())
				.meetAt(metadata.getMeetAt())
				.price(metadata.getPrice())
				.placeName(metadata.getPlaceName())
				.build();
	}

	private ChatRoom requireAccessibleRoom(String senderUuid, String roomId) {
		ChatRoom room = loadChatRoomPort.findById(roomId)
				.orElseThrow(ChatRoomNotFoundException::new);
		if (!room.hasParticipant(senderUuid)) {
			throw new ChatRoomAccessDeniedException();
		}
		return room;
	}

	private String requireMemberUuid(String memberUuid) {
		String normalized = memberUuid == null ? "" : memberUuid.trim();
		if (normalized.isBlank()) {
			throw new ChatAuthMissingException();
		}
		return normalized;
	}

	private String requireText(String value, String message) {
		String normalized = value == null ? "" : value.trim();
		if (normalized.isBlank()) {
			throw new InvalidChatRoomRequestException(message);
		}
		return normalized;
	}
}
