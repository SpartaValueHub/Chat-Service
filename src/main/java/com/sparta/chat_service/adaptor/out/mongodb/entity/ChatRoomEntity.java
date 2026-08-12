package com.sparta.chat_service.adaptor.out.mongodb.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// chat_rooms 컬렉션 문서
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "chat_rooms")
public class ChatRoomEntity {

	// MongoDB ObjectId
	@Id
	private String id;

	// 연관 상품(리스팅) UUID
	@Field("listing_uuid")
	private String listingUuid;

	// 참여자 목록
	@Field("participants")
	private List<ParticipantDocument> participants = new ArrayList<>();

	// 마지막 메시지 요약
	@Field("last_message")
	private LastMessageDocument lastMessage;

	// 채팅방 상태 (예: ACTIVE)
	@Field("status")
	private String status;

	// 생성 시각
	@CreatedDate
	@Field("created_at")
	private Instant createdAt;

	// 수정 시각
	@LastModifiedDate
	@Field("updated_at")
	private Instant updatedAt;

	@Builder
	private ChatRoomEntity(
			String id,
			String listingUuid,
			List<ParticipantDocument> participants,
			LastMessageDocument lastMessage,
			String status,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = id;
		this.listingUuid = listingUuid;
		this.participants = participants == null ? new ArrayList<>() : participants;
		this.lastMessage = lastMessage;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	// 마지막 메시지 요약 갱신
	public void updateLastMessage(LastMessageDocument lastMessage) {
		this.lastMessage = lastMessage;
		this.updatedAt = Instant.now();
	}
}
