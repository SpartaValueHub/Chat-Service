package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.dto.ChatRoomListItemDto;
import com.sparta.chat_service.application.port.in.dto.ChatRoomListResultDto;
import com.sparta.chat_service.application.port.out.LoadChatMessagePort;
import com.sparta.chat_service.application.port.out.LoadChatProductPostPort;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.domain.exception.ChatAuthMissingException;
import com.sparta.chat_service.domain.exception.InvalidChatRoomRequestException;
import com.sparta.chat_service.domain.model.ChatMessage;
import com.sparta.chat_service.domain.model.ChatProductPost;
import com.sparta.chat_service.domain.model.ChatRoom;
import com.sparta.chat_service.domain.model.ChatRoomStatus;
import com.sparta.chat_service.domain.model.LastMessage;
import com.sparta.chat_service.domain.model.MessageType;
import com.sparta.chat_service.domain.model.Participant;
import com.sparta.chat_service.domain.model.ProductPostStatus;
import com.sparta.chat_service.domain.model.TradeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListChatRoomsServiceTest {

	private static final String VIEWER_UUID = "22222222-2222-4222-8222-222222222222";
	private static final String SELLER_UUID = "33333333-3333-4333-8333-333333333333";
	private static final String OTHER_SELLER_UUID = "55555555-5555-4555-8555-555555555555";
	private static final String PRODUCT_POST_UUID = "11111111-1111-4111-8111-111111111111";
	private static final String OTHER_PRODUCT_POST_UUID = "44444444-4444-4444-8444-444444444444";

	private InMemoryChatRoomStore roomStore;
	private InMemoryChatProductPostStore productPostStore;
	private InMemoryChatMessageStore messageStore;
	private ListChatRoomsService service;

	@BeforeEach
	void setUp() {
		roomStore = new InMemoryChatRoomStore();
		productPostStore = new InMemoryChatProductPostStore();
		messageStore = new InMemoryChatMessageStore();
		service = new ListChatRoomsService(roomStore, productPostStore, messageStore);
	}

	@Test
	void list_rejectsMissingMemberHeader() {
		assertThrows(ChatAuthMissingException.class, () -> service.list("  "));
	}

	@Test
	void list_returnsEmptyRoomsWhenViewerHasNone() {
		ChatRoomListResultDto result = service.list(VIEWER_UUID);

		assertTrue(result.getRooms().isEmpty());
	}

	@Test
	void list_combinesProductSnapshotAndCounterpartUuid() {
		Instant updatedAt = Instant.parse("2026-08-19T04:00:00Z");
		roomStore.add(room(
				"room-1",
				PRODUCT_POST_UUID,
				VIEWER_UUID,
				SELLER_UUID,
				LastMessage.create("안녕하세요", updatedAt),
				updatedAt
		));
		productPostStore.add(ChatProductPost.create(
				PRODUCT_POST_UUID,
				"https://cdn.example.com/products/111.png",
				"중고 노트북",
				350000L,
				TradeStatus.SELLING
		));

		ChatRoomListItemDto item = service.list(VIEWER_UUID).getRooms().get(0);

		assertEquals("room-1", item.getRoomId());
		assertEquals("중고 노트북", item.getProductPost().getProductPostName());
		assertEquals(350000L, item.getProductPost().getPrice());
		assertEquals(ProductPostStatus.PUBLIC, item.getProductPost().getProductPostStatus());
		assertEquals(SELLER_UUID, item.getCounterpart().getMemberUuid());
		assertEquals("안녕하세요", item.getLastMessage().getContent());
		assertEquals(0, item.getUnreadCount());
	}

	@Test
	void list_keepsLastMessageNullAndUnreadZeroWhenRoomHasNoMessages() {
		roomStore.add(room("room-1", PRODUCT_POST_UUID, VIEWER_UUID, SELLER_UUID, null, Instant.parse("2026-08-19T04:00:00Z")));

		ChatRoomListItemDto item = service.list(VIEWER_UUID).getRooms().get(0);

		assertNull(item.getLastMessage());
		assertEquals(0, item.getUnreadCount());
	}

	@Test
	void list_doesNotFailWhenProductIsMissing() {
		roomStore.add(room("room-1", PRODUCT_POST_UUID, VIEWER_UUID, SELLER_UUID, null, Instant.parse("2026-08-19T04:00:00Z")));

		ChatRoomListResultDto result = service.list(VIEWER_UUID);
		ChatRoomListItemDto item = result.getRooms().get(0);

		assertEquals(1, result.getRooms().size());
		assertEquals(PRODUCT_POST_UUID, item.getProductPost().getProductPostUuid());
		assertNull(item.getProductPost().getProductPostName());
		assertEquals(SELLER_UUID, item.getCounterpart().getMemberUuid());
	}

	@Test
	void list_sortsByLastMessageThenUpdatedAt() {
		Instant older = Instant.parse("2026-08-19T01:00:00Z");
		Instant newer = Instant.parse("2026-08-19T03:00:00Z");
		Instant latestUpdate = Instant.parse("2026-08-19T04:00:00Z");
		roomStore.add(room("room-old", PRODUCT_POST_UUID, VIEWER_UUID, SELLER_UUID, LastMessage.create("이전", older), older));
		roomStore.add(room(
				"room-no-message",
				OTHER_PRODUCT_POST_UUID,
				VIEWER_UUID,
				OTHER_SELLER_UUID,
				null,
				latestUpdate
		));
		roomStore.add(room("room-new", PRODUCT_POST_UUID, VIEWER_UUID, SELLER_UUID, LastMessage.create("최근", newer), newer));

		List<ChatRoomListItemDto> rooms = service.list(VIEWER_UUID).getRooms();

		assertEquals("room-new", rooms.get(0).getRoomId());
		assertEquals("room-old", rooms.get(1).getRoomId());
		assertEquals("room-no-message", rooms.get(2).getRoomId());
	}

	@Test
	void list_countsUnreadAfterLastReadExcludingOwnMessages() {
		Instant first = Instant.parse("2026-08-19T01:00:00Z");
		Instant second = Instant.parse("2026-08-19T02:00:00Z");
		Instant own = Instant.parse("2026-08-19T03:00:00Z");
		roomStore.add(room(
				"room-1",
				PRODUCT_POST_UUID,
				VIEWER_UUID,
				SELLER_UUID,
				LastMessage.create("내 말", own),
				own,
				first
		));
		messageStore.add(ChatMessage.restore("m1", "room-1", SELLER_UUID, MessageType.TEXT, "하나", null, first));
		messageStore.add(ChatMessage.restore("m2", "room-1", SELLER_UUID, MessageType.TEXT, "둘", null, second));
		messageStore.add(ChatMessage.restore("m3", "room-1", VIEWER_UUID, MessageType.TEXT, "내 말", null, own));

		assertEquals(1, service.list(VIEWER_UUID).getRooms().get(0).getUnreadCount());
	}

	@Test
	void listByProductPost_rejectsMissingMemberHeader() {
		assertThrows(ChatAuthMissingException.class, () -> service.listByProductPost("  ", PRODUCT_POST_UUID));
	}

	@Test
	void listByProductPost_rejectsBlankProductPostUuid() {
		assertThrows(InvalidChatRoomRequestException.class, () -> service.listByProductPost(SELLER_UUID, "  "));
	}

	@Test
	void listByProductPost_returnsEmptyWhenViewerHasNoRoomForProduct() {
		roomStore.add(room("room-other", OTHER_PRODUCT_POST_UUID, VIEWER_UUID, OTHER_SELLER_UUID, null, Instant.parse("2026-08-19T04:00:00Z")));

		ChatRoomListResultDto result = service.listByProductPost(VIEWER_UUID, PRODUCT_POST_UUID);

		assertTrue(result.getRooms().isEmpty());
	}

	@Test
	void listByProductPost_returnsOnlyRoomsForProductThatViewerJoined() {
		Instant older = Instant.parse("2026-08-19T01:00:00Z");
		Instant newer = Instant.parse("2026-08-19T03:00:00Z");
		roomStore.add(room("room-buyer-1", PRODUCT_POST_UUID, VIEWER_UUID, SELLER_UUID, LastMessage.create("이전", older), older));
		roomStore.add(room("room-other-product", OTHER_PRODUCT_POST_UUID, VIEWER_UUID, OTHER_SELLER_UUID, LastMessage.create("다른 상품", newer), newer));
		roomStore.add(room("room-buyer-2", PRODUCT_POST_UUID, OTHER_SELLER_UUID, SELLER_UUID, LastMessage.create("최근", newer), newer));
		productPostStore.add(ChatProductPost.create(
				PRODUCT_POST_UUID,
				"https://cdn.example.com/products/111.png",
				"중고 노트북",
				350000L,
				TradeStatus.SELLING
		));

		List<ChatRoomListItemDto> rooms = service.listByProductPost(SELLER_UUID, PRODUCT_POST_UUID).getRooms();

		assertEquals(2, rooms.size());
		assertEquals("room-buyer-2", rooms.get(0).getRoomId());
		assertEquals("room-buyer-1", rooms.get(1).getRoomId());
		assertEquals(VIEWER_UUID, rooms.get(1).getCounterpart().getMemberUuid());
		assertEquals(OTHER_SELLER_UUID, rooms.get(0).getCounterpart().getMemberUuid());
		assertEquals("중고 노트북", rooms.get(0).getProductPost().getProductPostName());
	}

	private ChatRoom room(
			String id,
			String productPostUuid,
			String memberUuid1,
			String memberUuid2,
			LastMessage lastMessage,
			Instant updatedAt
	) {
		return room(id, productPostUuid, memberUuid1, memberUuid2, lastMessage, updatedAt, null);
	}

	private ChatRoom room(
			String id,
			String productPostUuid,
			String memberUuid1,
			String memberUuid2,
			LastMessage lastMessage,
			Instant updatedAt,
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
				lastMessage,
				ChatRoomStatus.ACTIVE,
				joinedAt,
				updatedAt
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
					.filter(room -> hasMember(room, memberUuid))
					.toList();
		}

		@Override
		public List<ChatRoom> findByParticipantAndProductPost(String memberUuid, String productPostUuid) {
			return rooms.stream()
					.filter(room -> productPostUuid.equals(room.getProductPostUuid()))
					.filter(room -> hasMember(room, memberUuid))
					.toList();
		}

		private boolean hasMember(ChatRoom room, String memberUuid) {
			return room.getParticipants().stream()
					.anyMatch(participant -> memberUuid.equals(participant.getMemberUuid()));
		}
	}

	private static final class InMemoryChatProductPostStore implements LoadChatProductPostPort {

		private final Map<String, ChatProductPost> posts = new HashMap<>();

		void add(ChatProductPost productPost) {
			posts.put(productPost.getProductPostUuid(), productPost);
		}

		@Override
		public List<ChatProductPost> findAllByProductPostUuids(Collection<String> productPostUuids) {
			return productPostUuids.stream()
					.map(posts::get)
					.filter(post -> post != null)
					.toList();
		}

		@Override
		public Optional<ChatProductPost> findByProductPostUuid(String productPostUuid) {
			return Optional.ofNullable(posts.get(productPostUuid));
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
