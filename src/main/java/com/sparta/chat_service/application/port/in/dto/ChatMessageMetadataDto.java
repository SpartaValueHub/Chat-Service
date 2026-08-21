package com.sparta.chat_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// 메시지 타입별 부가 정보 (TEXT는 null)
@Getter
@Builder
public class ChatMessageMetadataDto {

	private final String fileSize;
	private final Integer imageWidth;
	private final Integer imageHeight;
	private final String reservationId;
	private final Instant meetAt;
	private final Long price;
	private final String placeName;
	private final Double latitude;
	private final Double longitude;
}
