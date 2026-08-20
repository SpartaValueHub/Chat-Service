package com.sparta.chat_service.adaptor.in.websocket.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.sparta.chat_service.domain.model.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

// STOMP 메시지 전송 본문
@Getter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SendChatMessageRequestVo {

	private MessageType messageType;
	private String content;
	private Metadata metadata;

	@Getter
	@NoArgsConstructor
	@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
	public static class Metadata {

		private String fileSize;
		private Integer imageWidth;
		private Integer imageHeight;
		private String reservationId;
		private Instant meetAt;
		private Long price;
		private String placeName;
	}
}
