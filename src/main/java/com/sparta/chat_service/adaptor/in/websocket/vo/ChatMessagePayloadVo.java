package com.sparta.chat_service.adaptor.in.websocket.vo;

import com.sparta.chat_service.domain.model.MessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

// STOMP로 브로드캐스트하는 저장된 메시지
@Getter
@Builder
public class ChatMessagePayloadVo {

	private final String messageId;
	private final String roomId;
	private final String senderUuid;
	private final MessageType messageType;
	private final String content;
	private final Metadata metadata;
	private final OffsetDateTime createdAt;

	@Getter
	@Builder
	public static class Metadata {

		private final String fileSize;
		private final Integer imageWidth;
		private final Integer imageHeight;
		private final String reservationId;
		private final OffsetDateTime meetAt;
		private final Long price;
		private final String placeName;
		private final Double latitude;
		private final Double longitude;
	}
}
