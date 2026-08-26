package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.ConsumeReservationEventUseCase;
import com.sparta.chat_service.application.port.in.dto.ChatMessageItemDto;
import com.sparta.chat_service.application.port.in.dto.ChatMessageMetadataDto;
import com.sparta.chat_service.application.port.in.dto.ReservationEventCommandDto;
import com.sparta.chat_service.application.port.out.ChatRoomPresencePort;
import com.sparta.chat_service.application.port.out.LoadChatMessagePort;
import com.sparta.chat_service.application.port.out.LoadChatProductPostPort;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.application.port.out.PublishChatListPreviewPort;
import com.sparta.chat_service.application.port.out.PublishChatRoomMessagePort;
import com.sparta.chat_service.application.port.out.SaveChatMessagePort;
import com.sparta.chat_service.application.port.out.SaveChatProductPostPort;
import com.sparta.chat_service.application.port.out.UpdateChatRoomLastMessagePort;
import com.sparta.chat_service.application.port.out.UpdateParticipantLastReadPort;
import com.sparta.chat_service.application.port.out.dto.ChatListPreviewDto;
import com.sparta.chat_service.domain.model.ChatMessage;
import com.sparta.chat_service.domain.model.ChatProductPost;
import com.sparta.chat_service.domain.model.ChatRoom;
import com.sparta.chat_service.domain.model.LastMessage;
import com.sparta.chat_service.domain.model.MessageMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumeReservationEventService implements ConsumeReservationEventUseCase {

	static final String CREATED = "CREATED";
	static final String RESERVATION_CONTENT = "거래가 예약되었습니다";

	private final LoadChatRoomPort loadChatRoomPort;
	private final SaveChatMessagePort saveChatMessagePort;
	private final UpdateChatRoomLastMessagePort updateChatRoomLastMessagePort;
	private final UpdateParticipantLastReadPort updateParticipantLastReadPort;
	private final ChatRoomPresencePort chatRoomPresencePort;
	private final LoadChatMessagePort loadChatMessagePort;
	private final PublishChatListPreviewPort publishChatListPreviewPort;
	private final PublishChatRoomMessagePort publishChatRoomMessagePort;
	private final LoadChatProductPostPort loadChatProductPostPort;
	private final SaveChatProductPostPort saveChatProductPostPort;

	@Override
	public void consume(ReservationEventCommandDto command) {
		if (command == null || isBlank(command.getEventType())) {
			return;
		}
		if (!CREATED.equals(command.getEventType())) {
			log.debug("reservation.events {} no-op", command.getEventType());
			return;
		}
		String roomId = trim(command.getChatRoomUuid());
		String sellerUuid = trim(command.getSellerUuid());
		String reservationUuid = trim(command.getReservationUuid());
		if (isBlank(roomId) || isBlank(sellerUuid) || isBlank(reservationUuid)) {
			log.warn("reservation.events CREATED skipped: chatRoomUuid/sellerUuid/reservationUuid missing");
			return;
		}
		ChatRoom room = loadChatRoomPort.findById(roomId).orElse(null);
		if (room == null) {
			log.warn("reservation.events CREATED skipped: chat room not found roomId={}", roomId);
			return;
		}
		ChatProductPost productPost = loadProductPost(room, command.getProductPostUuid());
		ChatMessage saved = saveChatMessagePort.save(ChatMessage.createReservation(
				room.getId(),
				sellerUuid,
				RESERVATION_CONTENT,
				MessageMetadata.ofReservation(
						reservationUuid,
						parseInstant(command.getMeetAt()),
						productPost == null ? null : productPost.getPrice(),
						blankToNull(command.getPlaceName())
				)
		));
		LastMessage lastMessage = LastMessage.create(saved.getContent(), saved.getCreatedAt());
		updateChatRoomLastMessagePort.updateLastMessage(room.getId(), lastMessage);
		markHeaderReserved(productPost);
		markReadForViewers(room, sellerUuid, saved.getCreatedAt());
		publishListPreviews(room, sellerUuid, lastMessage);
		publishChatRoomMessagePort.publish(room.getId(), toItem(saved));
	}

	private ChatProductPost loadProductPost(ChatRoom room, String eventProductPostUuid) {
		String productPostUuid = !isBlank(eventProductPostUuid) ? trim(eventProductPostUuid) : room.getProductPostUuid();
		if (isBlank(productPostUuid)) {
			return null;
		}
		return loadChatProductPostPort.findByProductPostUuid(productPostUuid).orElse(null);
	}

	private void markHeaderReserved(ChatProductPost productPost) {
		if (productPost == null) {
			log.warn("reservation.events CREATED: product snapshot missing, header skipped");
			return;
		}
		saveChatProductPostPort.save(productPost.markReserved());
	}

	private void markReadForViewers(ChatRoom room, String senderUuid, Instant lastReadAt) {
		updateParticipantLastReadPort.updateLastRead(room.getId(), senderUuid, lastReadAt);
		for (String memberUuid : room.participantUuids()) {
			if (senderUuid.equals(memberUuid)) {
				continue;
			}
			if (chatRoomPresencePort.isViewing(memberUuid, room.getId())) {
				updateParticipantLastReadPort.updateLastRead(room.getId(), memberUuid, lastReadAt);
			}
		}
	}

	private void publishListPreviews(ChatRoom room, String senderUuid, LastMessage lastMessage) {
		for (String memberUuid : room.participantUuids()) {
			publishChatListPreviewPort.publish(memberUuid, ChatListPreviewDto.builder()
					.roomId(room.getId())
					.lastMessage(ChatListPreviewDto.LastMessage.builder()
							.content(lastMessage.getContent())
							.createdAt(lastMessage.getCreatedAt())
							.build())
					.unreadCount(unreadCountFor(room, senderUuid, memberUuid))
					.updatedAt(lastMessage.getCreatedAt())
					.build());
		}
	}

	private int unreadCountFor(ChatRoom room, String senderUuid, String memberUuid) {
		if (senderUuid.equals(memberUuid) || chatRoomPresencePort.isViewing(memberUuid, room.getId())) {
			return 0;
		}
		Instant lastReadAt = room.lastReadAt(memberUuid).orElse(null);
		return loadChatMessagePort.countUnread(room.getId(), memberUuid, lastReadAt);
	}

	private ChatMessageItemDto toItem(ChatMessage message) {
		MessageMetadata metadata = message.getMetadata();
		return ChatMessageItemDto.builder()
				.messageId(message.getId())
				.senderUuid(message.getSenderUuid())
				.messageType(message.getMessageType())
				.content(message.getContent())
				.metadata(metadata == null ? null : ChatMessageMetadataDto.builder()
						.reservationId(metadata.getReservationId())
						.meetAt(metadata.getMeetAt())
						.price(metadata.getPrice())
						.placeName(metadata.getPlaceName())
						.build())
				.createdAt(message.getCreatedAt())
				.build();
	}

	private Instant parseInstant(String value) {
		String normalized = blankToNull(value);
		if (normalized == null) {
			return null;
		}
		try {
			return Instant.parse(normalized);
		} catch (Exception ignored) {
			try {
				return OffsetDateTime.parse(normalized).toInstant();
			} catch (Exception exception) {
				log.warn("reservation.events meetAt parse failed value={}", normalized);
				return null;
			}
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private String trim(String value) {
		return value == null ? null : value.trim();
	}

	private String blankToNull(String value) {
		if (isBlank(value)) {
			return null;
		}
		return value.trim();
	}
}
