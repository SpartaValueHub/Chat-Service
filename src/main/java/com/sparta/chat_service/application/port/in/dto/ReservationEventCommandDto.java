package com.sparta.chat_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

// reservation.events 1건. JSON 키와 같은 이름.
@Getter
@Builder
public class ReservationEventCommandDto {

	private final String eventType;
	private final String productPostUuid;
	private final String reservationUuid;
	private final String chatRoomUuid;
	private final String meetAt;
	private final String placeName;
	private final String sellerUuid;
	private final String buyerUuid;
	private final String updatedAt;
}
