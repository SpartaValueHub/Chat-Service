package com.sparta.chat_service.adaptor.out.mongodb.mapper;

import com.sparta.chat_service.adaptor.out.mongodb.entity.ChatMessageEntity;
import com.sparta.chat_service.adaptor.out.mongodb.entity.ChatRoomEntity;
import com.sparta.chat_service.adaptor.out.mongodb.entity.LastMessageDocument;
import com.sparta.chat_service.adaptor.out.mongodb.entity.MessageMetadataDocument;
import com.sparta.chat_service.adaptor.out.mongodb.entity.ParticipantDocument;
import com.sparta.chat_service.domain.model.ChatMessage;
import com.sparta.chat_service.domain.model.ChatRoom;
import com.sparta.chat_service.domain.model.ChatRoomStatus;
import com.sparta.chat_service.domain.model.LastMessage;
import com.sparta.chat_service.domain.model.MessageMetadata;
import com.sparta.chat_service.domain.model.MessageType;
import com.sparta.chat_service.domain.model.Participant;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

// 도메인 <-> MongoDB 문서 매핑
@Component
public class ChatMongoMapper {

	public ChatRoomEntity toEntity(ChatRoom chatRoom) {
		return ChatRoomEntity.builder()
				.id(chatRoom.getId())
				.productPostUuid(chatRoom.getProductPostUuid())
				.sellerUuid(chatRoom.getSellerUuid())
				.participants(toParticipantDocuments(chatRoom.getParticipants()))
				.lastMessage(toLastMessageDocument(chatRoom.getLastMessage()))
				.status(chatRoom.getStatus() != null ? chatRoom.getStatus().name() : null)
				.createdAt(chatRoom.getCreatedAt())
				.updatedAt(chatRoom.getUpdatedAt())
				.build();
	}

	public ChatRoom toDomain(ChatRoomEntity entity) {
		return ChatRoom.restore(
				entity.getId(),
				entity.getProductPostUuid(),
				entity.getSellerUuid(),
				toParticipants(entity.getParticipants()),
				toLastMessage(entity.getLastMessage()),
				entity.getStatus() != null ? ChatRoomStatus.valueOf(entity.getStatus()) : null,
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}

	public ChatMessageEntity toEntity(ChatMessage chatMessage) {
		return ChatMessageEntity.builder()
				.id(chatMessage.getId())
				.roomId(chatMessage.getRoomId())
				.senderUuid(chatMessage.getSenderUuid())
				.messageType(chatMessage.getMessageType() != null ? chatMessage.getMessageType().name() : null)
				.content(chatMessage.getContent())
				.metadata(toMetadataDocument(chatMessage.getMetadata()))
				.createdAt(chatMessage.getCreatedAt())
				.build();
	}

	public ChatMessage toDomain(ChatMessageEntity entity) {
		return ChatMessage.restore(
				entity.getId(),
				entity.getRoomId(),
				entity.getSenderUuid(),
				entity.getMessageType() != null ? MessageType.valueOf(entity.getMessageType()) : null,
				entity.getContent(),
				toMetadata(entity.getMetadata()),
				entity.getCreatedAt()
		);
	}

	private List<ParticipantDocument> toParticipantDocuments(List<Participant> participants) {
		if (participants == null || participants.isEmpty()) {
			return Collections.emptyList();
		}
		return participants.stream()
				.map(participant -> ParticipantDocument.builder()
						.memberUuid(participant.getMemberUuid())
						.inRoom(participant.isInRoom())
						.joinedAt(participant.getJoinedAt())
						.lastReadAt(participant.getLastReadAt())
						.build())
				.toList();
	}

	private List<Participant> toParticipants(List<ParticipantDocument> documents) {
		if (documents == null || documents.isEmpty()) {
			return Collections.emptyList();
		}
		return documents.stream()
				.map(document -> Participant.restore(
						document.getMemberUuid(),
						Boolean.TRUE.equals(document.getInRoom()),
						document.getJoinedAt(),
						document.getLastReadAt()
				))
				.toList();
	}

	private LastMessageDocument toLastMessageDocument(LastMessage lastMessage) {
		if (lastMessage == null) {
			return null;
		}
		return LastMessageDocument.builder()
				.content(lastMessage.getContent())
				.createdAt(lastMessage.getCreatedAt())
				.build();
	}

	private LastMessage toLastMessage(LastMessageDocument document) {
		if (document == null) {
			return null;
		}
		return LastMessage.restore(document.getContent(), document.getCreatedAt());
	}

	private MessageMetadataDocument toMetadataDocument(MessageMetadata metadata) {
		if (metadata == null) {
			return null;
		}
		return MessageMetadataDocument.builder()
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

	private MessageMetadata toMetadata(MessageMetadataDocument document) {
		if (document == null) {
			return null;
		}
		return MessageMetadata.restore(
				document.getFileSize(),
				document.getImageWidth(),
				document.getImageHeight(),
				document.getReservationId(),
				document.getMeetAt(),
				document.getPrice(),
				document.getPlaceName(),
				document.getLatitude(),
				document.getLongitude()
		);
	}
}
