package com.sparta.chat_service.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// 채팅방 도메인
@Getter
public class ChatRoom {

	// MongoDB 문서 ID
	private final String id;
	// 연관 상품 게시글 UUID
	private final String productPostUuid;
	// 상품을 올린 판매자 UUID (상세 상단 닉네임)
	private final String sellerUuid;
	// 참여자 목록
	private final List<Participant> participants;
	// 마지막 메시지 요약
	private final LastMessage lastMessage;
	// 채팅방 상태
	private final ChatRoomStatus status;
	// 생성 시각
	private final Instant createdAt;
	// 수정 시각
	private final Instant updatedAt;

	@Builder
	private ChatRoom(
			String id,
			String productPostUuid,
			String sellerUuid,
			List<Participant> participants,
			LastMessage lastMessage,
			ChatRoomStatus status,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = id;
		this.productPostUuid = productPostUuid;
		this.sellerUuid = sellerUuid;
		this.participants = participants == null
				? List.of()
				: Collections.unmodifiableList(new ArrayList<>(participants));
		this.lastMessage = lastMessage;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static ChatRoom create(String productPostUuid, String sellerUuid, List<Participant> participants) {
		Instant now = Instant.now();
		return ChatRoom.builder()
				.productPostUuid(productPostUuid)
				.sellerUuid(sellerUuid)
				.participants(participants)
				.lastMessage(null)
				.status(ChatRoomStatus.ACTIVE)
				.createdAt(now)
				.updatedAt(now)
				.build();
	}

	public static ChatRoom restore(
			String id,
			String productPostUuid,
			String sellerUuid,
			List<Participant> participants,
			LastMessage lastMessage,
			ChatRoomStatus status,
			Instant createdAt,
			Instant updatedAt
	) {
		return ChatRoom.builder()
				.id(id)
				.productPostUuid(productPostUuid)
				.sellerUuid(sellerUuid)
				.participants(participants)
				.lastMessage(lastMessage)
				.status(status)
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.build();
	}

	// 이 방의 참여자인지
	public boolean hasParticipant(String memberUuid) {
		if (memberUuid == null || memberUuid.isBlank()) {
			return false;
		}
		return participants.stream()
				.anyMatch(participant -> memberUuid.equals(participant.getMemberUuid()));
	}

	// 1:1 방에서 나 이외 참여자 UUID
	public Optional<String> counterpartUuid(String memberUuid) {
		if (memberUuid == null || memberUuid.isBlank()) {
			return Optional.empty();
		}
		return participants.stream()
				.map(Participant::getMemberUuid)
				.filter(participantUuid -> !memberUuid.equals(participantUuid))
				.findFirst();
	}

	// 마지막 메시지 요약 갱신
	public ChatRoom updateLastMessage(LastMessage lastMessage) {
		return ChatRoom.builder()
				.id(this.id)
				.productPostUuid(this.productPostUuid)
				.sellerUuid(this.sellerUuid)
				.participants(this.participants)
				.lastMessage(lastMessage)
				.status(this.status)
				.createdAt(this.createdAt)
				.updatedAt(Instant.now())
				.build();
	}
}
