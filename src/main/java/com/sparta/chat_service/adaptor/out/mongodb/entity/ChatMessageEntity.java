package com.sparta.chat_service.adaptor.out.mongodb.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

// chat_messages 컬렉션 문서
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "chat_messages")
public class ChatMessageEntity {

	// MongoDB ObjectId
	@Id
	private String id;

	// 채팅방 ID
	@Indexed
	@Field("room_id")
	private String roomId;

	// 발신자 회원 UUID
	@Field("sender_uuid")
	private String senderUuid;

	// 메시지 유형 (TEXT / IMAGE / RESERVATION)
	@Field("message_type")
	private String messageType;

	// 본문 또는 이미지 URL
	@Field("content")
	private String content;

	// 타입별 부가 정보 (TEXT는 null)
	@Field("metadata")
	private MessageMetadataDocument metadata;

	// 생성 시각
	@CreatedDate
	@Field("created_at")
	private Instant createdAt;

	@Builder
	private ChatMessageEntity(
			String id,
			String roomId,
			String senderUuid,
			String messageType,
			String content,
			MessageMetadataDocument metadata,
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
}
