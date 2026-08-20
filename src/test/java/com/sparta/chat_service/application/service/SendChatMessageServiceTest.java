package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.dto.ChatMessageItemDto;
import com.sparta.chat_service.application.port.in.dto.ChatMessageMetadataDto;
import com.sparta.chat_service.application.port.in.dto.SendChatMessageCommandDto;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.application.port.out.PublishChatListPreviewPort;
import com.sparta.chat_service.application.port.out.SaveChatMessagePort;
import com.sparta.chat_service.application.port.out.UpdateChatRoomLastMessagePort;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
	private RecordingChatListPublisher listPublisher;
	private SendChatMessageService service;

	@BeforeEach
	void setUp() {
		roomStore = new InMemoryChatRoomStore();
		messageStore = new InMemoryChatMessageStore();
		listPublisher = new RecordingChatListPublisher();
		service = new SendChatMessageService(roomStore, messageStore, roomStore, listPublisher);
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
	void send_savesTextAndUpdatesLastMessage() {
		ChatMessageItemDto result = service.send(command(SENDER_UUID, "안녕하세요", MessageType.TEXT, null));

		assertEquals("msg-1", result.getMessageId());
		assertEquals(SENDER_UUID, result.getSenderUuid());
		assertEquals(MessageType.TEXT, result.getMessageType());
		assertEquals("안녕하세요", result.getContent());
		assertNull(result.getMetadata());
		assertEquals(1, messageStore.messages.size());
		assertEquals("안녕하세요", roomStore.lastMessage.getContent());
		assertEquals(1, listPublisher.published.size());
		assertEquals(List.of(SENDER_UUID, SELLER_UUID), listPublisher.published.get(0).memberUuids);
		assertEquals("안녕하세요", listPublisher.published.get(0).preview.getLastMessage().getContent());
		assertEquals(0, listPublisher.published.get(0).preview.getUnreadCount());
		assertEquals(ROOM_ID, listPublisher.published.get(0).preview.getRoomId());
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
		assertEquals("사진", roomStore.lastMessage.getContent());
		assertEquals("사진", listPublisher.published.get(0).preview.getLastMessage().getContent());
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

	private static final class InMemoryChatRoomStore implements LoadChatRoomPort, UpdateChatRoomLastMessagePort {

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
		public void updateLastMessage(String roomId, LastMessage lastMessage) {
			this.lastMessage = lastMessage;
		}
	}

	private static final class InMemoryChatMessageStore implements SaveChatMessagePort {

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
	}

	private static final class RecordingChatListPublisher implements PublishChatListPreviewPort {

		private final List<Published> published = new ArrayList<>();

		@Override
		public void publish(List<String> memberUuids, ChatListPreviewDto preview) {
			published.add(new Published(List.copyOf(memberUuids), preview));
		}

		private static final class Published {
			private final List<String> memberUuids;
			private final ChatListPreviewDto preview;

			private Published(List<String> memberUuids, ChatListPreviewDto preview) {
				this.memberUuids = memberUuids;
				this.preview = preview;
			}
		}
	}
}
