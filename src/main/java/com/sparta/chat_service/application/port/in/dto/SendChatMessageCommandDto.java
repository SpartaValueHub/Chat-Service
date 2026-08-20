package com.sparta.chat_service.application.port.in.dto;

import com.sparta.chat_service.domain.model.MessageType;
import lombok.Builder;
import lombok.Getter;

// 채팅 메시지 전송 명령
@Getter
@Builder
public class SendChatMessageCommandDto {

	private final String senderUuid;
	private final String roomId;
	private final MessageType messageType;
	private final String content;
	private final ChatMessageMetadataDto metadata;
}
