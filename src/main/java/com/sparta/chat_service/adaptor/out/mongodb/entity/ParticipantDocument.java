package com.sparta.chat_service.adaptor.out.mongodb.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

// chat_rooms.participants 임베디드 문서
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParticipantDocument {

	// 회원 UUID
	@Field("member_uuid")
	private String memberUuid;

	// 현재 방 입장 여부
	@Field("is_in_room")
	private Boolean inRoom;

	// 참여 시각
	@Field("joined_at")
	private Instant joinedAt;

	// 이 참여자가 읽은 마지막 메시지 시각
	@Field("last_read_at")
	private Instant lastReadAt;

	@Builder
	private ParticipantDocument(String memberUuid, Boolean inRoom, Instant joinedAt, Instant lastReadAt) {
		this.memberUuid = memberUuid;
		this.inRoom = inRoom;
		this.joinedAt = joinedAt;
		this.lastReadAt = lastReadAt;
	}
}
