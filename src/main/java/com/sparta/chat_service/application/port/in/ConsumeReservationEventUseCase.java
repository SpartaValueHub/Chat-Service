package com.sparta.chat_service.application.port.in;

import com.sparta.chat_service.application.port.in.dto.ReservationEventCommandDto;

// reservation.events 소비. 1차는 CREATED만 처리.
public interface ConsumeReservationEventUseCase {

	void consume(ReservationEventCommandDto command);
}
