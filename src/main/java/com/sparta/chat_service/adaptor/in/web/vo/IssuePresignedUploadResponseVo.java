package com.sparta.chat_service.adaptor.in.web.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

// 채팅 이미지 Presigned PUT URL 발급 응답
@Getter
@Builder
public class IssuePresignedUploadResponseVo {

	private final String uploadUrl;
	private final String method;
	private final Map<String, String> headers;
	private final String s3Key;
	private final String publicUrl;
	private final int expiresInSeconds;
}
