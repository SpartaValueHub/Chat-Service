package com.sparta.chat_service.adaptor.in.websocket.controller;

import com.sparta.chat_service.adaptor.in.SeoulDateTimes;
import com.sparta.chat_service.adaptor.in.web.vo.ErrorResponseVo;
import com.sparta.chat_service.adaptor.in.websocket.vo.ChatMessagePayloadVo;
import com.sparta.chat_service.adaptor.in.websocket.vo.SendChatMessageRequestVo;
import com.sparta.chat_service.application.port.in.SendChatMessageUseCase;
import com.sparta.chat_service.application.port.in.dto.ChatMessageItemDto;
import com.sparta.chat_service.application.port.in.dto.ChatMessageMetadataDto;
import com.sparta.chat_service.application.port.in.dto.SendChatMessageCommandDto;
import com.sparta.chat_service.domain.exception.ChatAuthMissingException;
import com.sparta.chat_service.domain.exception.ChatRoomAccessDeniedException;
import com.sparta.chat_service.domain.exception.ChatRoomNotFoundException;
import com.sparta.chat_service.domain.exception.InvalidChatRoomRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;

// STOMP 메시지 전송. 저장 후 /topic/chat.{roomId} 구독자에게 전달
@Controller
@RequiredArgsConstructor
public class StompChatController {

	private final SendChatMessageUseCase sendChatMessageUseCase;

	@MessageMapping("/chat.{roomId}")
	@SendTo("/topic/chat.{roomId}")
	public ChatMessagePayloadVo send(
			@DestinationVariable String roomId,
			SendChatMessageRequestVo requestVo,
			Principal principal
	) {
		if (requestVo == null) {
			throw new InvalidChatRoomRequestException("요청 본문이 필요합니다.");
		}
		ChatMessageItemDto saved = sendChatMessageUseCase.send(toCommand(roomId, requestVo, principal));
		return toPayload(roomId, saved);
	}

	@MessageExceptionHandler(ChatAuthMissingException.class)
	@SendToUser("/queue/errors")
	public ErrorResponseVo handleAuthMissing(ChatAuthMissingException exception) {
		return error(HttpStatus.UNAUTHORIZED, exception.getCode(), exception.getMessage());
	}

	@MessageExceptionHandler(InvalidChatRoomRequestException.class)
	@SendToUser("/queue/errors")
	public ErrorResponseVo handleInvalid(InvalidChatRoomRequestException exception) {
		return error(HttpStatus.BAD_REQUEST, exception.getCode(), exception.getMessage());
	}

	@MessageExceptionHandler(ChatRoomNotFoundException.class)
	@SendToUser("/queue/errors")
	public ErrorResponseVo handleNotFound(ChatRoomNotFoundException exception) {
		return error(HttpStatus.NOT_FOUND, exception.getCode(), exception.getMessage());
	}

	@MessageExceptionHandler(ChatRoomAccessDeniedException.class)
	@SendToUser("/queue/errors")
	public ErrorResponseVo handleDenied(ChatRoomAccessDeniedException exception) {
		return error(HttpStatus.FORBIDDEN, exception.getCode(), exception.getMessage());
	}

	private SendChatMessageCommandDto toCommand(
			String roomId,
			SendChatMessageRequestVo requestVo,
			Principal principal
	) {
		SendChatMessageRequestVo.Metadata metadata = requestVo.getMetadata();
		return SendChatMessageCommandDto.builder()
				.senderUuid(principal == null ? null : principal.getName())
				.roomId(roomId)
				.messageType(requestVo.getMessageType())
				.content(requestVo.getContent())
				.metadata(metadata == null ? null : ChatMessageMetadataDto.builder()
						.fileSize(metadata.getFileSize())
						.imageWidth(metadata.getImageWidth())
						.imageHeight(metadata.getImageHeight())
						.reservationId(metadata.getReservationId())
						.meetAt(metadata.getMeetAt())
						.price(metadata.getPrice())
						.placeName(metadata.getPlaceName())
						.latitude(metadata.getLatitude())
						.longitude(metadata.getLongitude())
						.build())
				.build();
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

	private ErrorResponseVo error(HttpStatus status, String code, String message) {
		return ErrorResponseVo.builder()
				.timestamp(SeoulDateTimes.toSeoul(Instant.now()))
				.status(status.value())
				.code(code)
				.message(message)
				.path("/app/chat")
				.build();
	}
}
