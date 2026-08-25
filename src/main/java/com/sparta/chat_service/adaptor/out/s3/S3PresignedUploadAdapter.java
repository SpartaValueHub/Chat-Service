package com.sparta.chat_service.adaptor.out.s3;

import com.sparta.chat_service.application.port.out.IssuePresignedUploadPort;
import com.sparta.chat_service.application.port.out.dto.PresignedUploadDto;
import com.sparta.chat_service.config.ChatMediaProperties;
import com.sparta.chat_service.domain.exception.ChatImageUploadUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

// S3 Presigned PUT. Access Key 없이 DefaultCredentialsProvider (EC2 Role)
@Component
@RequiredArgsConstructor
public class S3PresignedUploadAdapter implements IssuePresignedUploadPort {

	private final S3Presigner s3Presigner;
	private final ChatMediaProperties chatMediaProperties;

	@Override
	public PresignedUploadDto issuePutUrl(String s3Key, String contentType, int expiresInSeconds) {
		String bucket = chatMediaProperties.getS3Bucket();
		if (bucket == null || bucket.isBlank()) {
			throw new ChatImageUploadUnavailableException("S3 버킷이 설정되지 않았습니다.");
		}
		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
					.bucket(bucket.trim())
					.key(s3Key)
					.contentType(contentType)
					.build();
			PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
					.signatureDuration(Duration.ofSeconds(expiresInSeconds))
					.putObjectRequest(putObjectRequest)
					.build();
			String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toExternalForm();
			return PresignedUploadDto.builder()
					.uploadUrl(uploadUrl)
					.s3Key(s3Key)
					.expiresInSeconds(expiresInSeconds)
					.build();
		} catch (RuntimeException exception) {
			throw new ChatImageUploadUnavailableException("이미지 업로드 URL을 발급할 수 없습니다.", exception);
		}
	}
}
