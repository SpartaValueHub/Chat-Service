package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.dto.TotalUnreadCountResultDto;
import com.sparta.chat_service.application.port.out.LoadChatMessagePort;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.domain.exception.ChatAuthMissingException;
import com.sparta.chat_service.domain.model.ChatMessage;
import com.sparta.chat_service.domain.model.ChatRoom;
import com.sparta.chat_service.domain.model.ChatRoomStatus;
import com.sparta.chat_service.domain.model.LastMessage;
import com.sparta.chat_service.domain.model.MessageType;
import com.sparta.chat_service.domain.model.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetTotalUnreadCountServiceTest {

	private static final String VIEWER_UUID = "22222222-2222-4222-8222-222222222222";
	private static final String SELLER_UUID = "33333333-3333-4333-8333-333333333333";
	private static final String OTHER_SELLER_UUID = "55555555-5555-4555-8555-555555555555";
	private static final String PRODUCT_POST_UUID = "11111111-1111-4111-8111-111111111111";
	private static final String OTHER_PRODUCT_POST_UUID = "44444444-4444-4444-8444-444444444444";

	private InMemoryChatRoomStore roomStore;
	private InMemoryChatMessageStore messageStore;
	private GetTotalUnreadCountService service;

	@BeforeEach
	void setUp() {
		roomStore = new InMemoryChatRoomStore();
		messageStore = new InMemoryChatMessageStore();
		service = new GetTotalUnreadCountService(roomStore, messageStore);
	}

	@Test
	void get_rejectsMissingMemberHeader() {
		assertThrows(ChatAuthMissingException.class, () -> service.get("  "));
	}

	@Test
	void get_returnsZeroWhenViewerHasNoRooms() {
		assertEquals(0, service.get(VIEWER_UUID).getTotalUnreadCount());
	}

	@Test
	void get_sumsUnreadAcrossRoomsExcludingOwnMessages() {
		Instant first = Instant.parse("2026-08-19T01:00:00Z");
		Instant second = Instant.parse("2026-08-19T02:00:00Z");
		Instant own = Instant.parse("2026-08-19T03:00:00Z");
		Instant otherRoom = Instant.parse("2026-08-19T04:00:00Z");
		roomStore.add(room("room-1", PRODUCT_POST_UUID, VIEWER_UUID, SELLER_UUID, first));
		roomStore.add(room("room-2", OTHER_PRODUCT_POST_UUID, VIEWER_UUID, OTHER_SELLER_UUID, null));
		messageStore.add(ChatMessage.restore("m1", "room-1", SELLER_UUID, MessageType.TEXT, "하나", null, first));
		messageStore.add(ChatMessage.restore("m2", "room-1", SELLER_UUID, MessageType.TEXT, "둘", null, second));
		messageStore.add(ChatMessage.restore("m3", "room-1", VIEWER_UUID, MessageType.TEXT, "내 말", null, own));
		messageStore.add(ChatMessage.restore("m4", "room-2", OTHER_SELLER_UUID, MessageType.TEXT, "다른 방", null, otherRoom));

		TotalUnreadCountResultDto result = service.get(VIEWER_UUID);

		assertEquals(2, result.getTotalUnreadCount());
	}

	private ChatRoom room(
			String id,
			String productPostUuid,
			String memberUuid1,
			String memberUuid2,
			Instant viewerLastReadAt
	) {
		Instant joinedAt = Instant.parse("2026-08-01T00:00:00Z");
		return ChatRoom.restore(
				id,
				productPostUuid,
				memberUuid2,
				List.of(
						Participant.restore(memberUuid1, true, joinedAt, viewerLastReadAt),
						Participant.join(memberUuid2, joinedAt)
				),
				LastMessage.create("미리보기", Instant.parse("2026-08-19T04:00:00Z")),
				ChatRoomStatus.ACTIVE,
				joinedAt,
				joinedAt
		);
	}

	private static final class InMemoryChatRoomStore implements LoadChatRoomPort {

		private final List<ChatRoom> rooms = new ArrayList<>();

		void add(ChatRoom room) {
			rooms.add(room);
		}

		@Override
		public Optional<ChatRoom> findByProductPostAndMembers(
				String productPostUuid,
				String memberUuid1,
				String memberUuid2
		) {
			return Optional.empty();
		}

		@Override
		public Optional<ChatRoom> findById(String roomId) {
			return rooms.stream()
					.filter(room -> roomId.equals(room.getId()))
					.findFirst();
		}

		@Override
		public List<ChatRoom> findByParticipant(String memberUuid) {
			return rooms.stream()
					.filter(room -> room.getParticipants().stream()
							.anyMatch(participant -> memberUuid.equals(participant.getMemberUuid())))
					.toList();
		}
	}

	private static final class InMemoryChatMessageStore implements LoadChatMessagePort {

		private final Map<String, ChatMessage> messages = new HashMap<>();

		void add(ChatMessage message) {
			messages.put(message.getId(), message);
		}

		@Override
		public Optional<ChatMessage> findById(String messageId) {
			return Optional.ofNullable(messages.get(messageId));
		}

		@Override
		public List<ChatMessage> findLatestByRoomId(String roomId, int limit) {
			return List.of();
		}

		@Override
		public List<ChatMessage> findByRoomIdBefore(String roomId, ChatMessage cursor, int limit) {
			return List.of();
		}

		@Override
		public int countUnread(String roomId, String viewerUuid, Instant lastReadAt) {
			return (int) messages.values().stream()
					.filter(message -> roomId.equals(message.getRoomId()))
					.filter(message -> !viewerUuid.equals(message.getSenderUuid()))
					.filter(message -> lastReadAt == null || message.getCreatedAt().isAfter(lastReadAt))
					.count();
		}
	}
}
