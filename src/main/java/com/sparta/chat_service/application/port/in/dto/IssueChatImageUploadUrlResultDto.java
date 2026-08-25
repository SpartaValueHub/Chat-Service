package com.sparta.chat_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

// 채팅 이미지 Presigned PUT URL 발급 결과
@Getter
@Builder
public class IssueChatImageUploadUrlResultDto {

	private final String uploadUrl;
	private final String method;
	private final String contentType;
	private final String s3Key;
	private final String publicUrl;
	private final int expiresInSeconds;
}
