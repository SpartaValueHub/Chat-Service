package com.sparta.chat_service.adaptor.out.mongodb.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

// chat_messages.metadata 임베디드 문서 (타입별 필드 선택 사용)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageMetadataDocument {

	// IMAGE: 파일 크기 표기
	@Field("file_size")
	private String fileSize;

	// IMAGE: 가로 픽셀
	@Field("image_width")
	private Integer imageWidth;

	// IMAGE: 세로 픽셀
	@Field("image_height")
	private Integer imageHeight;

	// RESERVATION: 예약 식별자
	@Field("reservation_id")
	private String reservationId;

	// RESERVATION: 약속 시각
	@Field("meet_at")
	private Instant meetAt;

	// RESERVATION: 거래 금액
	@Field("price")
	private Long price;

	// RESERVATION: 만남 장소명
	@Field("place_name")
	private String placeName;

	@Builder
	private MessageMetadataDocument(
			String fileSize,
			Integer imageWidth,
			Integer imageHeight,
			String reservationId,
			Instant meetAt,
			Long price,
			String placeName
	) {
		this.fileSize = fileSize;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.reservationId = reservationId;
		this.meetAt = meetAt;
		this.price = price;
		this.placeName = placeName;
	}
}
