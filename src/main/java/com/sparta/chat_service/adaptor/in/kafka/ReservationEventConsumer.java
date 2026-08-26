package com.sparta.chat_service.adaptor.in.kafka;

import com.sparta.chat_service.application.port.in.ConsumeReservationEventUseCase;
import com.sparta.chat_service.application.port.in.dto.ReservationEventCommandDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// reservation.events Consumer. group-id=chat-service. Producer 아님.
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventConsumer {

	static final String TOPIC = "reservation.events";
	static final String GROUP_ID = "chat-service";

	private final ConsumeReservationEventUseCase consumeReservationEventUseCase;

	@KafkaListener(topics = TOPIC, groupId = GROUP_ID)
	public void consume(ReservationEventPayload payload) {
		if (payload == null) {
			return;
		}
		try {
			consumeReservationEventUseCase.consume(ReservationEventCommandDto.builder()
					.eventType(payload.getEventType())
					.productPostUuid(payload.getProductPostUuid())
					.reservationUuid(payload.getReservationUuid())
					.chatRoomUuid(payload.getChatRoomUuid())
					.meetAt(payload.getMeetAt())
					.placeName(payload.getPlaceName())
					.sellerUuid(payload.getSellerUuid())
					.buyerUuid(payload.getBuyerUuid())
					.updatedAt(payload.getUpdatedAt())
					.build());
		} catch (Exception exception) {
			log.error("reservation.events consume failed eventType={} chatRoomUuid={}",
					payload.getEventType(), payload.getChatRoomUuid(), exception);
			throw exception;
		}
	}
}
