package com.sparta.chat_service.application.port.out;

import com.sparta.chat_service.application.port.out.dto.PresignedUploadDto;

// S3 Presigned PUT URL 발급
public interface IssuePresignedUploadPort {

	PresignedUploadDto issuePutUrl(String s3Key, String contentType, int expiresInSeconds);
}
