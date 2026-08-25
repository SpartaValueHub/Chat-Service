package com.sparta.chat_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

// S3 / CloudFront 채팅 이미지 설정. 자격증명은 EC2 Instance Role
@Getter
@Setter
@ConfigurationProperties(prefix = "app.media")
public class ChatMediaProperties {

	private String s3Bucket = "";
	private String cloudfrontBaseUrl = "";
	private String awsRegion = "ap-northeast-2";
	private long maxFileSizeBytes = 5_242_880L;
	private int presignExpireSeconds = 300;
}
