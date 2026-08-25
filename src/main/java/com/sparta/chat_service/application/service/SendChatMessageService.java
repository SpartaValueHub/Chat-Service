package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.SendChatMessageUseCase;
import com.sparta.chat_service.application.port.in.dto.ChatMessageItemDto;
import com.sparta.chat_service.application.port.in.dto.ChatMessageMetadataDto;
import com.sparta.chat_service.application.port.in.dto.SendChatMessageCommandDto;
import com.sparta.chat_service.application.port.out.ChatRoomPresencePort;
import com.sparta.chat_service.application.port.out.LoadChatMessagePort;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.application.port.out.PublishChatListPreviewPort;
import com.sparta.chat_service.application.port.out.SaveChatMessagePort;
import com.sparta.chat_service.application.port.out.UpdateChatRoomLastMessagePort;
import com.sparta.chat_service.application.port.out.UpdateParticipantLastReadPort;
import com.sparta.chat_service.application.port.out.dto.ChatListPreviewDto;
import com.sparta.chat_service.domain.exception.ChatAuthMissingException;
import com.sparta.chat_service.domain.exception.ChatRoomAccessDeniedException;
import com.sparta.chat_service.domain.exception.ChatRoomNotFoundException;
import com.sparta.chat_service.domain.exception.InvalidChatRoomRequestException;
import com.sparta.chat_service.domain.model.ChatImageKey;
import com.sparta.chat_service.domain.model.ChatMessage;
import com.sparta.chat_service.domain.model.ChatRoom;
import com.sparta.chat_service.domain.model.LastMessage;
import com.sparta.chat_service.domain.model.MessageMetadata;
import com.sparta.chat_service.domain.model.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SendChatMessageService implements SendChatMessageUseCase {

	private static final String IMAGE_PREVIEW = "사진이 공유 되었습니다.";
	private static final String LOCATION_PREVIEW = "위치를 공유했습니다.";

	private final LoadChatRoomPort loadChatRoomPort;
	private final SaveChatMessagePort saveChatMessagePort;
	private final UpdateChatRoomLastMessagePort updateChatRoomLastMessagePort;
	private final UpdateParticipantLastReadPort updateParticipantLastReadPort;
	private final ChatRoomPresencePort chatRoomPresencePort;
	private final LoadChatMessagePort loadChatMessagePort;
	private final PublishChatListPreviewPort publishChatListPreviewPort;
	private final ChatImageUrlResolver chatImageUrlResolver;

	@Override
	public ChatMessageItemDto send(SendChatMessageCommandDto command) {
		if (command == null) {
			throw new InvalidChatRoomRequestException("요청 본문이 필요합니다.");
		}
		String senderUuid = requireMemberUuid(command.getSenderUuid());
		String roomId = requireText(command.getRoomId(), "roomId는 필수입니다.");
		ChatRoom room = requireAccessibleRoom(senderUuid, roomId);
		ChatMessage saved = saveChatMessagePort.save(toNewMessage(room.getId(), senderUuid, command));
		LastMessage lastMessage = LastMessage.create(preview(saved), saved.getCreatedAt());
		updateChatRoomLastMessagePort.updateLastMessage(room.getId(), lastMessage);
		markReadForViewers(room, senderUuid, saved.getCreatedAt());
		publishListPreviews(room, senderUuid, lastMessage);
		return toItem(saved);
	}

	private void markReadForViewers(ChatRoom room, String senderUuid, Instant lastReadAt) {
		updateParticipantLastReadPort.updateLastRead(room.getId(), senderUuid, lastReadAt);
		for (String memberUuid : room.participantUuids()) {
			if (senderUuid.equals(memberUuid)) {
				continue;
			}
			if (chatRoomPresencePort.isViewing(memberUuid, room.getId())) {
				updateParticipantLastReadPort.updateLastRead(room.getId(), memberUuid, lastReadAt);
			}
		}
	}

	private void publishListPreviews(ChatRoom room, String senderUuid, LastMessage lastMessage) {
		for (String memberUuid : room.participantUuids()) {
			publishChatListPreviewPort.publish(memberUuid, toListPreview(
					room.getId(),
					lastMessage,
					unreadCountFor(room, senderUuid, memberUuid)
			));
		}
	}

	private int unreadCountFor(ChatRoom room, String senderUuid, String memberUuid) {
		if (senderUuid.equals(memberUuid) || chatRoomPresencePort.isViewing(memberUuid, room.getId())) {
			return 0;
		}
		Instant lastReadAt = room.lastReadAt(memberUuid).orElse(null);
		return loadChatMessagePort.countUnread(room.getId(), memberUuid, lastReadAt);
	}

	private ChatMessage toNewMessage(String roomId, String senderUuid, SendChatMessageCommandDto command) {
		MessageType messageType = command.getMessageType() == null ? MessageType.TEXT : command.getMessageType();
		if (messageType == MessageType.LOCATION) {
			return ChatMessage.createLocation(roomId, senderUuid, toLocationMetadata(command.getMetadata()));
		}
		String content = requireText(command.getContent(), "content는 필수입니다.");
		if (messageType == MessageType.TEXT) {
			return ChatMessage.createText(roomId, senderUuid, content);
		}
		if (messageType == MessageType.IMAGE) {
			return ChatMessage.createImage(
					roomId,
					senderUuid,
					requireImageContent(content),
					toImageMetadata(command.getMetadata())
			);
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

	private MessageMetadata toLocationMetadata(ChatMessageMetadataDto metadataDto) {
		if (metadataDto == null || metadataDto.getLatitude() == null || metadataDto.getLongitude() == null) {
			throw new InvalidChatRoomRequestException("latitude와 longitude는 필수입니다.");
		}
		Double latitude = metadataDto.getLatitude();
		Double longitude = metadataDto.getLongitude();
		if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
			throw new InvalidChatRoomRequestException("위도·경도 범위가 올바르지 않습니다.");
		}
		return MessageMetadata.ofLocation(latitude, longitude, blankToNull(metadataDto.getPlaceName()));
	}

	private ChatListPreviewDto toListPreview(String roomId, LastMessage lastMessage, int unreadCount) {
		return ChatListPreviewDto.builder()
				.roomId(roomId)
				.lastMessage(ChatListPreviewDto.LastMessage.builder()
						.content(lastMessage.getContent())
						.createdAt(lastMessage.getCreatedAt())
						.build())
				.unreadCount(unreadCount)
				.updatedAt(lastMessage.getCreatedAt())
				.build();
	}

	private String preview(ChatMessage message) {
		if (message.getMessageType() == MessageType.IMAGE) {
			return IMAGE_PREVIEW;
		}
		if (message.getMessageType() == MessageType.LOCATION) {
			return LOCATION_PREVIEW;
		}
		return message.getContent();
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

	private String requireImageContent(String content) {
		if (ChatImageKey.isHttpUrl(content) || ChatImageKey.isValidS3Key(content)) {
			return content.trim();
		}
		throw new InvalidChatRoomRequestException("이미지 키가 올바르지 않습니다. Presigned API의 s3Key를 사용하세요.");
	}

	private String requireText(String value, String message) {
		String normalized = value == null ? "" : value.trim();
		if (normalized.isBlank()) {
			throw new InvalidChatRoomRequestException(message);
		}
		return normalized;
	}

	private String blankToNull(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim();
		return normalized.isBlank() ? null : normalized;
	}
}
