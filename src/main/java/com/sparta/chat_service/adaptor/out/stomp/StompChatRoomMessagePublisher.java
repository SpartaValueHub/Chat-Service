package com.sparta.chat_service.adaptor.out.stomp;

import com.sparta.chat_service.adaptor.in.SeoulDateTimes;
import com.sparta.chat_service.adaptor.in.websocket.vo.ChatMessagePayloadVo;
import com.sparta.chat_service.application.port.in.dto.ChatMessageItemDto;
import com.sparta.chat_service.application.port.in.dto.ChatMessageMetadataDto;
import com.sparta.chat_service.application.port.out.PublishChatRoomMessagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

// Kafka insert 말풍선을 /topic/chat.{roomId} 구독자에게 전달
@Component
@RequiredArgsConstructor
public class StompChatRoomMessagePublisher implements PublishChatRoomMessagePort {

	private final SimpMessagingTemplate messagingTemplate;

	@Override
	public void publish(String roomId, ChatMessageItemDto message) {
		if (roomId == null || roomId.isBlank() || message == null) {
			return;
		}
		messagingTemplate.convertAndSend("/topic/chat." + roomId.trim(), toPayload(roomId.trim(), message));
	}

	private ChatMessagePayloadVo toPayload(String roomId, ChatMessageItemDto itemDto) {
		return ChatMessagePayloadVo.builder()
				.messageId(itemDto.getMessageId())
				.roomId(roomId)
				.senderUuid(itemDto.getSenderUuid())
				.messageType(itemDto.getMessageType())
				.content(itemDto.getContent())
				.metadata(toMetadata(itemDto.getMetadata()))
				.createdAt(SeoulDateTimes.toSeoul(itemDto.getCreatedAt()))
				.build();
	}

	private ChatMessagePayloadVo.Metadata toMetadata(ChatMessageMetadataDto metadataDto) {
		if (metadataDto == null) {
			return null;
		}
		return ChatMessagePayloadVo.Metadata.builder()
				.fileSize(metadataDto.getFileSize())
				.imageWidth(metadataDto.getImageWidth())
				.imageHeight(metadataDto.getImageHeight())
				.reservationId(metadataDto.getReservationId())
				.meetAt(SeoulDateTimes.toSeoul(metadataDto.getMeetAt()))
				.price(metadataDto.getPrice())
				.placeName(metadataDto.getPlaceName())
				.latitude(metadataDto.getLatitude())
				.longitude(metadataDto.getLongitude())
				.build();
	}
}
