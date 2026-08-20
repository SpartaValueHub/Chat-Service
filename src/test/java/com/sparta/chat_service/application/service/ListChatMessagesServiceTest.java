package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.dto.ChatMessageItemDto;
import com.sparta.chat_service.application.port.out.LoadChatMessagePort;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.domain.exception.ChatAuthMissingException;
import com.sparta.chat_service.domain.exception.ChatRoomAccessDeniedException;
import com.sparta.chat_service.domain.exception.ChatRoomNotFoundException;
import com.sparta.chat_service.domain.exception.InvalidChatRoomRequestException;
import com.sparta.chat_service.domain.model.ChatMessage;
import com.sparta.chat_service.domain.model.ChatRoom;
import com.sparta.chat_service.domain.model.ChatRoomStatus;
import com.sparta.chat_service.domain.model.MessageType;
import com.sparta.chat_service.domain.model.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListChatMessagesServiceTest {

	private static final String VIEWER_UUID = "22222222-2222-4222-8222-222222222222";
	private static final String SELLER_UUID = "33333333-3333-4333-8333-333333333333";
	private static final String STRANGER_UUID = "99999999-9999-4999-8999-999999999999";
	private static final String PRODUCT_POST_UUID = "11111111-1111-4111-8111-111111111111";
	private static final String ROOM_ID = "room-1";

	private InMemoryChatRoomStore roomStore;
	private InMemoryChatMessageStore messageStore;
	private ListChatMessagesService service;

	@BeforeEach
	void setUp() {
		roomStore = new InMemoryChatRoomStore();
		messageStore = new InMemoryChatMessageStore();
		service = new ListChatMessagesService(roomStore, messageStore);
		roomStore.add(room());
	}

	@Test
	void list_rejectsMissingMemberHeader() {
		assertThrows(ChatAuthMissingException.class, () -> service.list("  ", ROOM_ID, null, null));
	}

	@Test
	void list_rejectsUnknownRoom() {
		assertThrows(ChatRoomNotFoundException.class, () -> service.list(VIEWER_UUID, "missing", null, null));
	}

	@Test
	void list_rejectsNonParticipant() {
		assertThrows(ChatRoomAccessDeniedException.class, () -> service.list(STRANGER_UUID, ROOM_ID, null, null));
	}

	@Test
	void list_returnsEmptyWhenRoomHasNoMessages() {
		assertTrue(service.list(VIEWER_UUID, ROOM_ID, null, null).getMessages().isEmpty());
	}

	@Test
	void list_returnsLatestPageOldestFirst() {
		messageStore.add(text("m1", VIEWER_UUID, "첫 말", Instant.parse("2026-08-19T01:00:00Z")));
		messageStore.add(text("m2", SELLER_UUID, "둘째", Instant.parse("2026-08-19T02:00:00Z")));
		messageStore.add(text("m3", VIEWER_UUID, "셋째", Instant.parse("2026-08-19T03:00:00Z")));

		List<ChatMessageItemDto> messages = service.list(VIEWER_UUID, ROOM_ID, null, 2).getMessages();

		assertEquals(2, messages.size());
		assertEquals("m2", messages.get(0).getMessageId());
		assertEquals("m3", messages.get(1).getMessageId());
		assertEquals(MessageType.TEXT, messages.get(0).getMessageType());
	}

	@Test
	void list_readsOlderPageWithBeforeCursor() {
		messageStore.add(text("m1", VIEWER_UUID, "첫 말", Instant.parse("2026-08-19T01:00:00Z")));
		messageStore.add(text("m2", SELLER_UUID, "둘째", Instant.parse("2026-08-19T02:00:00Z")));
		messageStore.add(text("m3", VIEWER_UUID, "셋째", Instant.parse("2026-08-19T03:00:00Z")));

		List<ChatMessageItemDto> messages = service.list(VIEWER_UUID, ROOM_ID, "m2", 10).getMessages();

		assertEquals(1, messages.size());
		assertEquals("m1", messages.get(0).getMessageId());
	}

	@Test
	void list_rejectsUnknownBeforeCursor() {
		assertThrows(InvalidChatRoomRequestException.class, () -> service.list(VIEWER_UUID, ROOM_ID, "missing", null));
	}

	@Test
	void list_rejectsInvalidLimit() {
		assertThrows(InvalidChatRoomRequestException.class, () -> service.list(VIEWER_UUID, ROOM_ID, null, 0));
	}

	private ChatRoom room() {
		Instant joinedAt = Instant.parse("2026-08-01T00:00:00Z");
		return ChatRoom.restore(
				ROOM_ID,
				PRODUCT_POST_UUID,
				SELLER_UUID,
				List.of(Participant.join(VIEWER_UUID, joinedAt), Participant.join(SELLER_UUID, joinedAt)),
				null,
				ChatRoomStatus.ACTIVE,
				joinedAt,
				joinedAt
		);
	}

	private ChatMessage text(String id, String senderUuid, String content, Instant createdAt) {
		return ChatMessage.restore(id, ROOM_ID, senderUuid, MessageType.TEXT, content, null, createdAt);
	}

	private static final class InMemoryChatRoomStore implements LoadChatRoomPort {

		private final Map<String, ChatRoom> rooms = new HashMap<>();

		void add(ChatRoom room) {
			rooms.put(room.getId(), room);
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
			return Optional.ofNullable(rooms.get(roomId));
		}

		@Override
		public List<ChatRoom> findByParticipant(String memberUuid) {
			return List.of();
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
			List<ChatMessage> newestFirst = roomMessages(roomId);
			newestFirst.sort(Comparator.comparing(ChatMessage::getCreatedAt)
					.thenComparing(ChatMessage::getId)
					.reversed());
			return reverse(newestFirst.stream().limit(limit).toList());
		}

		@Override
		public List<ChatMessage> findByRoomIdBefore(String roomId, ChatMessage cursor, int limit) {
			List<ChatMessage> older = roomMessages(roomId).stream()
					.filter(message -> isBefore(message, cursor))
					.sorted(Comparator.comparing(ChatMessage::getCreatedAt)
							.thenComparing(ChatMessage::getId)
							.reversed())
					.limit(limit)
					.toList();
			return reverse(older);
		}

		private List<ChatMessage> roomMessages(String roomId) {
			return new ArrayList<>(messages.values().stream()
					.filter(message -> roomId.equals(message.getRoomId()))
					.toList());
		}

		private boolean isBefore(ChatMessage message, ChatMessage cursor) {
			int compared = message.getCreatedAt().compareTo(cursor.getCreatedAt());
			if (compared < 0) {
				return true;
			}
			if (compared > 0) {
				return false;
			}
			return message.getId().compareTo(cursor.getId()) < 0;
		}

		private List<ChatMessage> reverse(List<ChatMessage> newestFirst) {
			List<ChatMessage> oldestFirst = new ArrayList<>(newestFirst);
			java.util.Collections.reverse(oldestFirst);
			return oldestFirst;
		}
	}
}
