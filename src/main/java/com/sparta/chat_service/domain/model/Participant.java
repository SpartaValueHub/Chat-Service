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
	// 이 참여자가 읽은 마지막 메시지 시각
	private final Instant lastReadAt;

	@Builder
	private Participant(String memberUuid, boolean inRoom, Instant joinedAt, Instant lastReadAt) {
		this.memberUuid = memberUuid;
		this.inRoom = inRoom;
		this.joinedAt = joinedAt;
		this.lastReadAt = lastReadAt;
	}

	public static Participant join(String memberUuid, Instant joinedAt) {
		return Participant.builder()
				.memberUuid(memberUuid)
				.inRoom(true)
				.joinedAt(joinedAt)
				.lastReadAt(null)
				.build();
	}

	public static Participant restore(String memberUuid, boolean inRoom, Instant joinedAt) {
		return restore(memberUuid, inRoom, joinedAt, null);
	}

	public static Participant restore(String memberUuid, boolean inRoom, Instant joinedAt, Instant lastReadAt) {
		return Participant.builder()
				.memberUuid(memberUuid)
				.inRoom(inRoom)
				.joinedAt(joinedAt)
				.lastReadAt(lastReadAt)
				.build();
	}

	// 방 퇴장
	public Participant leave() {
		return copy(false, this.lastReadAt);
	}

	// 방 재입장
	public Participant enter() {
		return copy(true, this.lastReadAt);
	}

	private Participant copy(boolean inRoom, Instant lastReadAt) {
		return Participant.builder()
				.memberUuid(this.memberUuid)
				.inRoom(inRoom)
				.joinedAt(this.joinedAt)
				.lastReadAt(lastReadAt)
				.build();
	}
}
