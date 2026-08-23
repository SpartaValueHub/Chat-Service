package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.dto.ChatMessageItemDto;
import com.sparta.chat_service.application.port.in.dto.ChatMessageMetadataDto;
import com.sparta.chat_service.application.port.in.dto.SendChatMessageCommandDto;
import com.sparta.chat_service.application.port.out.ChatRoomPresencePort;
import com.sparta.chat_service.application.port.out.LoadChatMessagePort;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.application.port.out.PublishChatListPreviewPort;
import com.sparta.chat_service.application.port.out.SaveChatMessagePort;
import com.sparta.chat_service.application.port.out.UpdateChatRoomLastMessagePort;
import com.sparta.chat_service.application.port.out.UpdateParticipantLastReadPort;
import com.sparta.chat_service.application.port.out.dto.ChatListPreviewDto;
import com.sparta.chat_service.domain.exception.ChatAuthMissingException;
import com.sparta.chat_service.domain.exception.ChatRoomAccessDeniedException;
import com.sparta.chat_service.domain.exception.ChatRoomNotFoundException;
import com.sparta.chat_service.domain.exception.InvalidChatRoomRequestException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SendChatMessageServiceTest {

	private static final String SENDER_UUID = "22222222-2222-4222-8222-222222222222";
	private static final String SELLER_UUID = "33333333-3333-4333-8333-333333333333";
	private static final String STRANGER_UUID = "99999999-9999-4999-8999-999999999999";
	private static final String PRODUCT_POST_UUID = "11111111-1111-4111-8111-111111111111";
	private static final String ROOM_ID = "room-1";

	private InMemoryChatRoomStore roomStore;
	private InMemoryChatMessageStore messageStore;
	private StubChatRoomPresence presence;
	private RecordingChatListPublisher listPublisher;
	private SendChatMessageService service;

	@BeforeEach
	void setUp() {
		roomStore = new InMemoryChatRoomStore();
		messageStore = new InMemoryChatMessageStore();
		presence = new StubChatRoomPresence();
		listPublisher = new RecordingChatListPublisher();
		service = new SendChatMessageService(
				roomStore,
				messageStore,
				roomStore,
				roomStore,
				presence,
				messageStore,
				listPublisher
		);
		roomStore.add(room());
	}

	@Test
	void send_rejectsMissingSender() {
		assertThrows(ChatAuthMissingException.class, () -> service.send(command("  ", "안녕", MessageType.TEXT, null)));
	}

	@Test
	void send_rejectsUnknownRoom() {
		SendChatMessageCommandDto command = SendChatMessageCommandDto.builder()
				.senderUuid(SENDER_UUID)
				.roomId("missing")
				.messageType(MessageType.TEXT)
				.content("안녕")
				.build();
		assertThrows(ChatRoomNotFoundException.class, () -> service.send(command));
	}

	@Test
	void send_rejectsNonParticipant() {
		assertThrows(ChatRoomAccessDeniedException.class,
				() -> service.send(command(STRANGER_UUID, "안녕", MessageType.TEXT, null)));
		assertEquals(0, listPublisher.published.size());
	}

	@Test
	void send_rejectsBlankContent() {
		assertThrows(InvalidChatRoomRequestException.class,
				() -> service.send(command(SENDER_UUID, "  ", MessageType.TEXT, null)));
	}

	@Test
	void send_savesTextAndPushesUnreadToOfflineCounterpart() {
		ChatMessageItemDto result = service.send(command(SENDER_UUID, "안녕하세요", MessageType.TEXT, null));

		assertEquals("msg-1", result.getMessageId());
		assertEquals(SENDER_UUID, result.getSenderUuid());
		assertEquals(MessageType.TEXT, result.getMessageType());
		assertEquals("안녕하세요", result.getContent());
		assertNull(result.getMetadata());
		assertEquals(1, messageStore.messages.size());
		assertEquals("안녕하세요", roomStore.lastMessage.getContent());
		assertEquals(2, listPublisher.published.size());
		assertEquals(0, listPublisher.unreadCount(SENDER_UUID));
		assertEquals(1, listPublisher.unreadCount(SELLER_UUID));
		assertEquals("안녕하세요", listPublisher.preview(SENDER_UUID).getLastMessage().getContent());
		assertEquals(ROOM_ID, listPublisher.preview(SENDER_UUID).getRoomId());
		assertEquals(result.getCreatedAt(), roomStore.lastReadAt(ROOM_ID, SENDER_UUID));
		assertNull(roomStore.lastReadAt(ROOM_ID, SELLER_UUID));
	}

	@Test
	void send_marksCounterpartReadWhenViewingRoom() {
		presence.view(SELLER_UUID, ROOM_ID);

		service.send(command(SENDER_UUID, "안녕하세요", MessageType.TEXT, null));

		assertEquals(0, listPublisher.unreadCount(SENDER_UUID));
		assertEquals(0, listPublisher.unreadCount(SELLER_UUID));
		assertEquals(roomStore.lastReadAt(ROOM_ID, SENDER_UUID), roomStore.lastReadAt(ROOM_ID, SELLER_UUID));
	}

	@Test
	void send_savesImageAndUsesPhotoPreview() {
		ChatMessageMetadataDto metadata = ChatMessageMetadataDto.builder()
				.fileSize("2.4MB")
				.imageWidth(800)
				.imageHeight(600)
				.build();

		ChatMessageItemDto result = service.send(command(
				SENDER_UUID,
				"https://cdn.example.com/chat/bag.png",
				MessageType.IMAGE,
				metadata
		));

		assertEquals(MessageType.IMAGE, result.getMessageType());
		assertEquals("https://cdn.example.com/chat/bag.png", result.getContent());
		assertEquals("2.4MB", result.getMetadata().getFileSize());
		assertEquals("사진이 공유 되었습니다.", roomStore.lastMessage.getContent());
		assertEquals("사진이 공유 되었습니다.", listPublisher.preview(SENDER_UUID).getLastMessage().getContent());
	}

	@Test
	void send_savesLocationWithNullContent() {
		ChatMessageMetadataDto metadata = ChatMessageMetadataDto.builder()
				.latitude(35.115)
				.longitude(129.042)
				.placeName("학원")
				.build();

		ChatMessageItemDto result = service.send(command(SENDER_UUID, null, MessageType.LOCATION, metadata));

		assertEquals(MessageType.LOCATION, result.getMessageType());
		assertNull(result.getContent());
		assertEquals(35.115, result.getMetadata().getLatitude());
		assertEquals(129.042, result.getMetadata().getLongitude());
		assertEquals("학원", result.getMetadata().getPlaceName());
		assertEquals("위치를 공유했습니다.", roomStore.lastMessage.getContent());
		assertEquals("위치를 공유했습니다.", listPublisher.preview(SENDER_UUID).getLastMessage().getContent());
	}

	@Test
	void send_rejectsLocationWithoutCoordinates() {
		assertThrows(InvalidChatRoomRequestException.class,
				() -> service.send(command(SENDER_UUID, null, MessageType.LOCATION, null)));
		assertEquals(0, listPublisher.published.size());
	}

	@Test
	void send_rejectsReservationType() {
		assertThrows(InvalidChatRoomRequestException.class,
				() -> service.send(command(SENDER_UUID, "예약", MessageType.RESERVATION, null)));
		assertEquals(0, listPublisher.published.size());
	}

	private SendChatMessageCommandDto command(
			String senderUuid,
			String content,
			MessageType messageType,
			ChatMessageMetadataDto metadata
	) {
		return SendChatMessageCommandDto.builder()
				.senderUuid(senderUuid)
				.roomId(ROOM_ID)
				.messageType(messageType)
				.content(content)
				.metadata(metadata)
				.build();
	}

	private ChatRoom room() {
		Instant joinedAt = Instant.parse("2026-08-01T00:00:00Z");
		return ChatRoom.restore(
				ROOM_ID,
				PRODUCT_POST_UUID,
				SELLER_UUID,
				List.of(Participant.join(SENDER_UUID, joinedAt), Participant.join(SELLER_UUID, joinedAt)),
				null,
				ChatRoomStatus.ACTIVE,
				joinedAt,
				joinedAt
		);
	}

	private static final class InMemoryChatRoomStore implements LoadChatRoomPort, UpdateChatRoomLastMessagePort,
			UpdateParticipantLastReadPort {

		private final Map<String, ChatRoom> rooms = new HashMap<>();
		private final Map<String, Instant> lastReads = new HashMap<>();
		private LastMessage lastMessage;

		void add(ChatRoom room) {
			rooms.put(room.getId(), room);
		}

		Instant lastReadAt(String roomId, String memberUuid) {
			return lastReads.get(roomId + "|" + memberUuid);
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
			lastReads.put(roomId + "|" + memberUuid, lastReadAt);
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
			return (int) messages.values().stream()
					.filter(message -> roomId.equals(message.getRoomId()))
					.filter(message -> !viewerUuid.equals(message.getSenderUuid()))
					.filter(message -> lastReadAt == null || message.getCreatedAt().isAfter(lastReadAt))
					.count();
		}
	}

	private static final class StubChatRoomPresence implements ChatRoomPresencePort {

		private final Set<String> viewing = new HashSet<>();

		void view(String memberUuid, String roomId) {
			viewing.add(memberUuid + "|" + roomId);
		}

		@Override
		public boolean isViewing(String memberUuid, String roomId) {
			return viewing.contains(memberUuid + "|" + roomId);
		}
	}

	private static final class RecordingChatListPublisher implements PublishChatListPreviewPort {

		private final List<Published> published = new ArrayList<>();

		@Override
		public void publish(String memberUuid, ChatListPreviewDto preview) {
			published.add(new Published(memberUuid, preview));
		}

		ChatListPreviewDto preview(String memberUuid) {
			return published.stream()
					.filter(item -> memberUuid.equals(item.memberUuid))
					.map(item -> item.preview)
					.findFirst()
					.orElseThrow();
		}

		int unreadCount(String memberUuid) {
			return preview(memberUuid).getUnreadCount();
		}

		private static final class Published {
			private final String memberUuid;
			private final ChatListPreviewDto preview;

			private Published(String memberUuid, ChatListPreviewDto preview) {
				this.memberUuid = memberUuid;
				this.preview = preview;
			}
		}
	}
}
