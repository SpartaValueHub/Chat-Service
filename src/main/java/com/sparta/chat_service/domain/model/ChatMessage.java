package com.sparta.chat_service.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// 채팅 메시지 도메인
@Getter
public class ChatMessage {

	// MongoDB 문서 ID
	private final String id;
	// 채팅방 ID
	private final String roomId;
	// 발신자 회원 UUID
	private final String senderUuid;
	// 메시지 유형
	private final MessageType messageType;
	// 본문 (TEXT/시스템문구) 또는 이미지 URL
	private final String content;
	// 타입별 부가 정보 (TEXT는 null)
	private final MessageMetadata metadata;
	// 생성 시각
	private final Instant createdAt;

	@Builder
	private ChatMessage(
			String id,
			String roomId,
			String senderUuid,
			MessageType messageType,
			String content,
			MessageMetadata metadata,
			Instant createdAt
	) {
		this.id = id;
		this.roomId = roomId;
		this.senderUuid = senderUuid;
		this.messageType = messageType;
		this.content = content;
		this.metadata = metadata;
		this.createdAt = createdAt;
	}

	public static ChatMessage createText(String roomId, String senderUuid, String content) {
		return ChatMessage.builder()
				.roomId(roomId)
				.senderUuid(senderUuid)
				.messageType(MessageType.TEXT)
				.content(content)
				.metadata(null)
				.createdAt(Instant.now())
				.build();
	}

	public static ChatMessage createImage(
			String roomId,
			String senderUuid,
			String imageUrl,
			MessageMetadata metadata
	) {
		return ChatMessage.builder()
				.roomId(roomId)
				.senderUuid(senderUuid)
				.messageType(MessageType.IMAGE)
				.content(imageUrl)
				.metadata(metadata)
				.createdAt(Instant.now())
				.build();
	}

	public static ChatMessage createReservation(
			String roomId,
			String senderUuid,
			String content,
			MessageMetadata metadata
	) {
		return ChatMessage.builder()
				.roomId(roomId)
				.senderUuid(senderUuid)
				.messageType(MessageType.RESERVATION)
				.content(content)
				.metadata(metadata)
				.createdAt(Instant.now())
				.build();
	}

	public static ChatMessage restore(
			String id,
			String roomId,
			String senderUuid,
			MessageType messageType,
			String content,
			MessageMetadata metadata,
			Instant createdAt
	) {
		return ChatMessage.builder()
				.id(id)
				.roomId(roomId)
				.senderUuid(senderUuid)
				.messageType(messageType)
				.content(content)
				.metadata(metadata)
				.createdAt(createdAt)
				.build();
	}
}
