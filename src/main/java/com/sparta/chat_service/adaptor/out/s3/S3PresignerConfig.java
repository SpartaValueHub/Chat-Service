package com.sparta.chat_service.adaptor.out.s3;

import com.sparta.chat_service.application.service.ChatImageUrlResolver;
import com.sparta.chat_service.config.ChatMediaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(ChatMediaProperties.class)
public class S3PresignerConfig {

	@Bean(destroyMethod = "close")
	public S3Presigner s3Presigner(ChatMediaProperties properties) {
		String region = properties.getAwsRegion();
		if (region == null || region.isBlank()) {
			region = "ap-northeast-2";
		}
		// 자격증명은 기본 체인 (EC2 Instance Role). Access Key 불필요
		return S3Presigner.builder()
				.region(Region.of(region))
				.build();
	}

	@Bean
	public ChatImageUrlResolver chatImageUrlResolver(ChatMediaProperties properties) {
		return new ChatImageUrlResolver(properties.getCloudfrontBaseUrl());
	}
}
