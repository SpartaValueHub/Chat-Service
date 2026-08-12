package com.sparta.chat_service.adaptor.out.mongodb.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

// chat_rooms.last_message 임베디드 문서
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LastMessageDocument {

	// 마지막 메시지 본문
	@Field("content")
	private String content;

	// 마지막 메시지 생성 시각
	@Field("created_at")
	private Instant createdAt;

	@Builder
	private LastMessageDocument(String content, Instant createdAt) {
		this.content = content;
		this.createdAt = createdAt;
	}
}
