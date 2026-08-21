package com.sparta.chat_service.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// 메시지 타입별 부가 정보 (TEXT는 null)
@Getter
public class MessageMetadata {

	// IMAGE: 파일 크기 표기 (예: 2.4MB)
	private final String fileSize;
	// IMAGE: 가로 픽셀
	private final Integer imageWidth;
	// IMAGE: 세로 픽셀
	private final Integer imageHeight;

	// RESERVATION: 예약 식별자
	private final String reservationId;
	// RESERVATION: 약속 시각
	private final Instant meetAt;
	// RESERVATION: 거래 금액
	private final Long price;
	// LOCATION / RESERVATION: 장소명
	private final String placeName;
	// LOCATION: 위도
	private final Double latitude;
	// LOCATION: 경도
	private final Double longitude;

	@Builder
	private MessageMetadata(
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

	public static MessageMetadata ofImage(String fileSize, Integer imageWidth, Integer imageHeight) {
		return MessageMetadata.builder()
				.fileSize(fileSize)
				.imageWidth(imageWidth)
				.imageHeight(imageHeight)
				.build();
	}

	public static MessageMetadata ofLocation(Double latitude, Double longitude, String placeName) {
		return MessageMetadata.builder()
				.latitude(latitude)
				.longitude(longitude)
				.placeName(placeName)
				.build();
	}

	public static MessageMetadata ofReservation(
			String reservationId,
			Instant meetAt,
			Long price,
			String placeName
	) {
		return MessageMetadata.builder()
				.reservationId(reservationId)
				.meetAt(meetAt)
				.price(price)
				.placeName(placeName)
				.build();
	}

	public static MessageMetadata restore(
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
		return MessageMetadata.builder()
				.fileSize(fileSize)
				.imageWidth(imageWidth)
				.imageHeight(imageHeight)
				.reservationId(reservationId)
				.meetAt(meetAt)
				.price(price)
				.placeName(placeName)
				.latitude(latitude)
				.longitude(longitude)
				.build();
	}
}
