package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.ListChatMessagesUseCase;
import com.sparta.chat_service.application.port.in.dto.ChatMessageItemDto;
import com.sparta.chat_service.application.port.in.dto.ChatMessageListResultDto;
import com.sparta.chat_service.application.port.in.dto.ChatMessageMetadataDto;
import com.sparta.chat_service.application.port.out.LoadChatMessagePort;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.domain.exception.ChatAuthMissingException;
import com.sparta.chat_service.domain.exception.ChatRoomAccessDeniedException;
import com.sparta.chat_service.domain.exception.ChatRoomNotFoundException;
import com.sparta.chat_service.domain.exception.InvalidChatRoomRequestException;
import com.sparta.chat_service.domain.model.ChatMessage;
import com.sparta.chat_service.domain.model.ChatRoom;
import com.sparta.chat_service.domain.model.MessageMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListChatMessagesService implements ListChatMessagesUseCase {

	private static final int DEFAULT_LIMIT = 50;
	private static final int MAX_LIMIT = 100;

	private final LoadChatRoomPort loadChatRoomPort;
	private final LoadChatMessagePort loadChatMessagePort;
	private final ChatImageUrlResolver chatImageUrlResolver;

	@Override
	public ChatMessageListResultDto list(String memberUuid, String roomId, String beforeMessageId, Integer limit) {
		String viewerUuid = requireMemberUuid(memberUuid);
		ChatRoom room = requireAccessibleRoom(viewerUuid, roomId);
		int pageSize = resolveLimit(limit);
		List<ChatMessage> messages = loadPage(room.getId(), beforeMessageId, pageSize);
		return ChatMessageListResultDto.builder()
				.messages(messages.stream().map(this::toItem).toList())
				.build();
	}

	private List<ChatMessage> loadPage(String roomId, String beforeMessageId, int limit) {
		String cursorId = beforeMessageId == null ? "" : beforeMessageId.trim();
		if (cursorId.isBlank()) {
			return loadChatMessagePort.findLatestByRoomId(roomId, limit);
		}
		ChatMessage cursor = loadChatMessagePort.findById(cursorId)
				.filter(message -> roomId.equals(message.getRoomId()))
				.orElseThrow(() -> new InvalidChatRoomRequestException("before 커서가 올바르지 않습니다."));
		return loadChatMessagePort.findByRoomIdBefore(roomId, cursor, limit);
	}

	private ChatMessageItemDto toItem(ChatMessage message) {
		return ChatMessageItemDto.builder()
				.messageId(message.getId())
				.senderUuid(message.getSenderUuid())
				.messageType(message.getMessageType())
				.content(chatImageUrlResolver.toResponseContent(message.getMessageType(), message.getContent()))
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
				.latitude(metadata.getLatitude())
				.longitude(metadata.getLongitude())
				.build();
	}

	private ChatRoom requireAccessibleRoom(String viewerUuid, String roomId) {
		String normalizedRoomId = requireRoomId(roomId);
		ChatRoom room = loadChatRoomPort.findById(normalizedRoomId)
				.orElseThrow(ChatRoomNotFoundException::new);
		if (!room.hasParticipant(viewerUuid)) {
			throw new ChatRoomAccessDeniedException();
		}
		return room;
	}

	private int resolveLimit(Integer limit) {
		if (limit == null) {
			return DEFAULT_LIMIT;
		}
		if (limit < 1 || limit > MAX_LIMIT) {
			throw new InvalidChatRoomRequestException("limit은 1 이상 " + MAX_LIMIT + " 이하여야 합니다.");
		}
		return limit;
	}

	private String requireMemberUuid(String memberUuid) {
		String normalized = memberUuid == null ? "" : memberUuid.trim();
		if (normalized.isBlank()) {
			throw new ChatAuthMissingException();
		}
		return normalized;
	}

	private String requireRoomId(String roomId) {
		String normalized = roomId == null ? "" : roomId.trim();
		if (normalized.isBlank()) {
			throw new InvalidChatRoomRequestException("roomId는 필수입니다.");
		}
		return normalized;
	}
}
