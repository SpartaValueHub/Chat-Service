package com.sparta.chat_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

// 채팅 이미지 Presigned PUT URL 발급 명령
@Getter
@Builder
public class IssueChatImageUploadUrlCommandDto {

	private final String memberUuid;
	private final String roomId;
	private final String contentType;
	private final Long fileSize;
}
