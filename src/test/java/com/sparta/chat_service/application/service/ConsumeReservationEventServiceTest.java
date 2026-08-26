package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.dto.ChatMessageItemDto;
import com.sparta.chat_service.application.port.in.dto.ReservationEventCommandDto;
import com.sparta.chat_service.application.port.out.LoadChatMessagePort;
import com.sparta.chat_service.application.port.out.LoadChatProductPostPort;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.application.port.out.PublishChatRoomMessagePort;
import com.sparta.chat_service.application.port.out.SaveChatMessagePort;
import com.sparta.chat_service.application.port.out.SaveChatProductPostPort;
import com.sparta.chat_service.application.port.out.UpdateChatRoomLastMessagePort;
import com.sparta.chat_service.application.port.out.UpdateParticipantLastReadPort;
import com.sparta.chat_service.domain.model.ChatMessage;
import com.sparta.chat_service.domain.model.ChatProductPost;
import com.sparta.chat_service.domain.model.ChatRoom;
import com.sparta.chat_service.domain.model.ChatRoomStatus;
import com.sparta.chat_service.domain.model.LastMessage;
import com.sparta.chat_service.domain.model.MessageType;
import com.sparta.chat_service.domain.model.Participant;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsumeReservationEventServiceTest {

	private static final String BUYER_UUID = "22222222-2222-4222-8222-222222222222";
	private static final String SELLER_UUID = "33333333-3333-4333-8333-333333333333";
	private static final String PRODUCT_POST_UUID = "11111111-1111-4111-8111-111111111111";
	private static final String ROOM_ID = "aaaaaaaaaaaaaaaaaaaaaaaa";
	private static final String RESERVATION_UUID = "44444444-4444-4444-8444-444444444444";

	private InMemoryChatRoomStore roomStore;
	private InMemoryChatMessageStore messageStore;
	private InMemoryChatProductPostStore productPostStore;
	private RecordingRoomPublisher roomPublisher;
	private ConsumeReservationEventService service;

	@BeforeEach
	void setUp() {
		roomStore = new InMemoryChatRoomStore();
		messageStore = new InMemoryChatMessageStore();
		productPostStore = new InMemoryChatProductPostStore();
		roomPublisher = new RecordingRoomPublisher();
		service = new ConsumeReservationEventService(
				roomStore,
				messageStore,
				roomStore,
				roomStore,
				(memberUuid, roomId) -> false,
				messageStore,
				(memberUuid, preview) -> {
				},
				roomPublisher,
				productPostStore,
				productPostStore
		);
		Instant joinedAt = Instant.parse("2026-08-01T00:00:00Z");
		roomStore.add(ChatRoom.restore(
				ROOM_ID,
				PRODUCT_POST_UUID,
				SELLER_UUID,
				List.of(Participant.join(BUYER_UUID, joinedAt), Participant.join(SELLER_UUID, joinedAt)),
				null,
				ChatRoomStatus.ACTIVE,
				joinedAt,
				joinedAt
		));
		productPostStore.add(ChatProductPost.create(
				PRODUCT_POST_UUID,
				"https://cdn.example.com/bag.png",
				"버버리 백",
				1_500_000L,
				TradeStatus.SELLING
		));
	}

	@Test
	void created_insertsReservationBubbleAndMarksHeaderReserved() {
		service.consume(created());

		assertEquals(1, messageStore.messages.size());
		ChatMessage saved = messageStore.messages.values().iterator().next();
		assertEquals(MessageType.RESERVATION, saved.getMessageType());
		assertEquals("거래가 예약되었습니다", saved.getContent());
		assertEquals(SELLER_UUID, saved.getSenderUuid());
		assertEquals(ROOM_ID, saved.getRoomId());
		assertEquals(RESERVATION_UUID, saved.getMetadata().getReservationId());
		assertEquals("해동병원 앞", saved.getMetadata().getPlaceName());
		assertEquals(Instant.parse("2026-08-26T03:00:00Z"), saved.getMetadata().getMeetAt());
		assertEquals(1_500_000L, saved.getMetadata().getPrice());
		assertEquals("거래가 예약되었습니다", roomStore.lastMessage.getContent());
		assertEquals(TradeStatus.RESERVED, productPostStore.posts.get(PRODUCT_POST_UUID).getTradeStatus());
		assertEquals(1, roomPublisher.published.size());
		assertEquals(ROOM_ID, roomPublisher.published.get(0).roomId);
		assertEquals(MessageType.RESERVATION, roomPublisher.published.get(0).message.getMessageType());
	}

	@Test
	void updated_isNoOp() {
		service.consume(event("UPDATED"));

		assertTrue(messageStore.messages.isEmpty());
		assertEquals(TradeStatus.SELLING, productPostStore.posts.get(PRODUCT_POST_UUID).getTradeStatus());
		assertTrue(roomPublisher.published.isEmpty());
	}

	@Test
	void canceled_isNoOp() {
		service.consume(event("CANCELED"));

		assertTrue(messageStore.messages.isEmpty());
		assertEquals(TradeStatus.SELLING, productPostStore.posts.get(PRODUCT_POST_UUID).getTradeStatus());
	}

	@Test
	void created_skipsMissingRoom() {
		service.consume(ReservationEventCommandDto.builder()
				.eventType("CREATED")
				.productPostUuid(PRODUCT_POST_UUID)
				.reservationUuid(RESERVATION_UUID)
				.chatRoomUuid("missing-room")
				.meetAt("2026-08-26T12:00:00+09:00")
				.placeName("해동병원 앞")
				.sellerUuid(SELLER_UUID)
				.buyerUuid(BUYER_UUID)
				.build());

		assertTrue(messageStore.messages.isEmpty());
		assertEquals(TradeStatus.SELLING, productPostStore.posts.get(PRODUCT_POST_UUID).getTradeStatus());
	}

	private ReservationEventCommandDto created() {
		return event("CREATED");
	}

	private ReservationEventCommandDto event(String eventType) {
		return ReservationEventCommandDto.builder()
				.eventType(eventType)
				.productPostUuid(PRODUCT_POST_UUID)
				.reservationUuid(RESERVATION_UUID)
				.chatRoomUuid(ROOM_ID)
				.meetAt("2026-08-26T12:00:00+09:00")
				.placeName("해동병원 앞")
				.sellerUuid(SELLER_UUID)
				.buyerUuid(BUYER_UUID)
				.updatedAt("2026-08-26T01:00:00Z")
				.build();
	}

	private static final class InMemoryChatRoomStore implements LoadChatRoomPort, UpdateChatRoomLastMessagePort,
			UpdateParticipantLastReadPort {

		private final Map<String, ChatRoom> rooms = new HashMap<>();
		private LastMessage lastMessage;

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

		@Override
		public List<ChatRoom> findByParticipantAndProductPost(String memberUuid, String productPostUuid) {
			return List.of();
		}

		@Override
		public void updateLastMessage(String roomId, LastMessage lastMessage) {
			this.lastMessage = lastMessage;
		}

		@Override
		public void updateLastRead(String roomId, String memberUuid, Instant lastReadAt) {
		}
	}

	private static final class InMemoryChatMessageStore implements SaveChatMessagePort, LoadChatMessagePort {

		private final Map<String, ChatMessage> messages = new HashMap<>();
		private final AtomicInteger saveCount = new AtomicInteger();

		@Override
		public ChatMessage save(ChatMessage chatMessage) {
			String id = "msg-" + saveCount.incrementAndGet();
			ChatMessage stored = ChatMessage.restore(
					id,
					chatMessage.getRoomId(),
					chatMessage.getSenderUuid(),
					chatMessage.getMessageType(),
					chatMessage.getContent(),
					chatMessage.getMetadata(),
					chatMessage.getCreatedAt()
			);
			messages.put(id, stored);
			return stored;
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
			return 0;
		}
	}

	private static final class InMemoryChatProductPostStore implements LoadChatProductPostPort, SaveChatProductPostPort {

		private final Map<String, ChatProductPost> posts = new HashMap<>();

		void add(ChatProductPost productPost) {
			posts.put(productPost.getProductPostUuid(), productPost);
		}

		@Override
		public List<ChatProductPost> findAllByProductPostUuids(Collection<String> productPostUuids) {
			return List.of();
		}

		@Override
		public Optional<ChatProductPost> findByProductPostUuid(String productPostUuid) {
			return Optional.ofNullable(posts.get(productPostUuid));
		}

		@Override
		public ChatProductPost save(ChatProductPost productPost) {
			posts.put(productPost.getProductPostUuid(), productPost);
			return productPost;
		}
	}

	private static final class RecordingRoomPublisher implements PublishChatRoomMessagePort {

		private final List<Published> published = new ArrayList<>();

		@Override
		public void publish(String roomId, ChatMessageItemDto message) {
			published.add(new Published(roomId, message));
		}

		private static final class Published {
			private final String roomId;
			private final ChatMessageItemDto message;

			private Published(String roomId, ChatMessageItemDto message) {
				this.roomId = roomId;
				this.message = message;
			}
		}
	}
}
