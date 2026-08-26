package com.sparta.chat_service.adaptor.in.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

// reservation.events JSON. 키 이름은 kafka.md 계약 그대로.
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservationEventPayload {

	private String eventType;
	private String productPostUuid;
	private String reservationUuid;
	private String chatRoomUuid;
	private String meetAt;
	private String placeName;
	private String sellerUuid;
	private String buyerUuid;
	private String updatedAt;
}
