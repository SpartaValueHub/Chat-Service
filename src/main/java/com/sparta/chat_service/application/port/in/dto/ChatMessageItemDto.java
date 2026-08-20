package com.sparta.chat_service.application.port.in.dto;

import com.sparta.chat_service.domain.model.MessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// 이력 한 건
@Getter
@Builder
public class ChatMessageItemDto {

	private final String messageId;
	private final String senderUuid;
	private final MessageType messageType;
	private final String content;
	private final ChatMessageMetadataDto metadata;
	private final Instant createdAt;
}
