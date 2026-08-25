package com.sparta.chat_service.application.port.out.dto;

import lombok.Builder;
import lombok.Getter;

// S3 Presigned PUT 발급 결과
@Getter
@Builder
public class PresignedUploadDto {

	private final String uploadUrl;
	private final String s3Key;
	private final int expiresInSeconds;
}
