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

	@Field("file_size")
	private String fileSize;

	@Field("image_width")
	private Integer imageWidth;

	@Field("image_height")
	private Integer imageHeight;

	@Field("reservation_id")
	private String reservationId;

	@Field("meet_at")
	private Instant meetAt;

	@Field("price")
	private Long price;

	@Field("place_name")
	private String placeName;

	@Field("latitude")
	private Double latitude;

	@Field("longitude")
	private Double longitude;

	@Builder
	private MessageMetadataDocument(
			String fileSize,
			Integer imageWidth,
			Integer imageHeight,
			String reservationId,
			Instant meetAt,
			Long price,
			String placeName,
			Double latitude,
			Double longitude
	) {
		this.fileSize = fileSize;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.reservationId = reservationId;
		this.meetAt = meetAt;
		this.price = price;
		this.placeName = placeName;
		this.latitude = latitude;
		this.longitude = longitude;
	}
}
