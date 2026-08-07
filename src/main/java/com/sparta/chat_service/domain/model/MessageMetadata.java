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
	// RESERVATION: 만남 장소명
	private final String placeName;

	@Builder
	private MessageMetadata(
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

	// 이미지 메시지 메타데이터 생성
	public static MessageMetadata ofImage(String fileSize, Integer imageWidth, Integer imageHeight) {
		return MessageMetadata.builder()
				.fileSize(fileSize)
				.imageWidth(imageWidth)
				.imageHeight(imageHeight)
				.build();
	}

	// 예약 메시지 메타데이터 생성
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

	// 영속 계층 복원용
	public static MessageMetadata restore(
			String fileSize,
			Integer imageWidth,
			Integer imageHeight,
			String reservationId,
			Instant meetAt,
			Long price,
			String placeName
	) {
		return MessageMetadata.builder()
				.fileSize(fileSize)
				.imageWidth(imageWidth)
				.imageHeight(imageHeight)
				.reservationId(reservationId)
				.meetAt(meetAt)
				.price(price)
				.placeName(placeName)
				.build();
	}
}
