package com.sparta.chat_service.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// 채팅방 목록용 마지막 메시지 요약
@Getter
public class LastMessage {

	// 마지막 메시지 본문
	private final String content;
	// 마지막 메시지 생성 시각
	private final Instant createdAt;

	@Builder
	private LastMessage(String content, Instant createdAt) {
		this.content = content;
		this.createdAt = createdAt;
	}

	public static LastMessage create(String content, Instant createdAt) {
		return LastMessage.builder()
				.content(content)
				.createdAt(createdAt)
				.build();
	}

	public static LastMessage restore(String content, Instant createdAt) {
		return create(content, createdAt);
	}
}
