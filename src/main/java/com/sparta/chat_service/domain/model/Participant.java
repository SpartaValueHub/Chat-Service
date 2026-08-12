package com.sparta.chat_service.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// 채팅방 참여자
@Getter
public class Participant {

	// 회원 UUID
	private final String memberUuid;
	// 현재 방 입장 여부
	private final boolean inRoom;
	// 참여 시각
	private final Instant joinedAt;

	@Builder
	private Participant(String memberUuid, boolean inRoom, Instant joinedAt) {
		this.memberUuid = memberUuid;
		this.inRoom = inRoom;
		this.joinedAt = joinedAt;
	}

	public static Participant join(String memberUuid, Instant joinedAt) {
		return Participant.builder()
				.memberUuid(memberUuid)
				.inRoom(true)
				.joinedAt(joinedAt)
				.build();
	}

	public static Participant restore(String memberUuid, boolean inRoom, Instant joinedAt) {
		return Participant.builder()
				.memberUuid(memberUuid)
				.inRoom(inRoom)
				.joinedAt(joinedAt)
				.build();
	}

	// 방 퇴장
	public Participant leave() {
		return Participant.builder()
				.memberUuid(this.memberUuid)
				.inRoom(false)
				.joinedAt(this.joinedAt)
				.build();
	}

	// 방 재입장
	public Participant enter() {
		return Participant.builder()
				.memberUuid(this.memberUuid)
				.inRoom(true)
				.joinedAt(this.joinedAt)
				.build();
	}
}
