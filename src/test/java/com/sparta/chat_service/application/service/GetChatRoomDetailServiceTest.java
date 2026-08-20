package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.ResolveChatUserProfileUseCase;
import com.sparta.chat_service.application.port.in.dto.ChatRoomDetailResultDto;
import com.sparta.chat_service.application.port.out.LoadChatMessagePort;
import com.sparta.chat_service.application.port.out.LoadChatProductPostPort;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.application.port.out.PublishChatListPreviewPort;
import com.sparta.chat_service.application.port.out.UpdateParticipantLastReadPort;
import com.sparta.chat_service.application.port.out.dto.ChatListPreviewDto;
import com.sparta.chat_service.domain.exception.ChatAuthMissingException;
import com.sparta.chat_service.domain.exception.ChatRoomAccessDeniedException;
import com.sparta.chat_service.domain.exception.ChatRoomNotFoundException;
import com.sparta.chat_service.domain.model.ChatProductPost;
import com.sparta.chat_service.domain.model.ChatRoom;
import com.sparta.chat_service.domain.model.ChatRoomStatus;
import com.sparta.chat_service.domain.model.ChatUserProfile;
import com.sparta.chat_service.domain.model.MemberGrade;
import com.sparta.chat_service.domain.model.Participant;
import com.sparta.chat_service.domain.model.TradeStatus;
import com.sparta.chat_service.domain.model.ChatMessage;
import com.sparta.chat_service.domain.model.MessageType;
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

class GetChatRoomDetailServiceTest {

	private static final String VIEWER_UUID = "22222222-2222-4222-8222-222222222222";
	private static final String SELLER_UUID = "33333333-3333-4333-8333-333333333333";
	private static final String STRANGER_UUID = "99999999-9999-4999-8999-999999999999";
	private static final String PRODUCT_POST_UUID = "11111111-1111-4111-8111-111111111111";
	private static final String ROOM_ID = "room-1";

	private InMemoryChatRoomStore roomStore;
	private InMemoryChatProductPostStore productPostStore;
	private InMemoryChatMessageStore messageStore;
	private RecordingLastReadStore lastReadStore;
	private RecordingChatListPublisher listPublisher;
	private StubResolveChatUserProfileUseCase resolveUseCase;
	private GetChatRoomDetailService service;

	@BeforeEach
	void setUp() {
		roomStore = new InMemoryChatRoomStore();
		productPostStore = new InMemoryChatProductPostStore();
		messageStore = new InMemoryChatMessageStore();
		lastReadStore = new RecordingLastReadStore();
		listPublisher = new RecordingChatListPublisher();
		resolveUseCase = new StubResolveChatUserProfileUseCase();
		service = new GetChatRoomDetailService(
				roomStore,
				productPostStore,
				messageStore,
				lastReadStore,
				listPublisher,
				resolveUseCase
		);
	}

	@Test
	void get_rejectsMissingMemberHeader() {
		assertThrows(ChatAuthMissingException.class, () -> service.get("  ", ROOM_ID));
	}

	@Test
	void get_rejectsUnknownRoom() {
		assertThrows(ChatRoomNotFoundException.class, () -> service.get(VIEWER_UUID, ROOM_ID));
	}

	@Test
	void get_rejectsNonParticipant() {
		roomStore.add(room());

		assertThrows(ChatRoomAccessDeniedException.class, () -> service.get(STRANGER_UUID, ROOM_ID));
	}

	@Test
	void get_combinesProductSnapshotAndSellerNicknameWithoutGrade() {
		roomStore.add(room());
		productPostStore.add(ChatProductPost.create(
				PRODUCT_POST_UUID,
				"https://cdn.example.com/products/111.png",
				"버버리 레더 포켓 미니 토트백",
				1500000L,
				TradeStatus.RESERVED
		));

		ChatRoomDetailResultDto result = service.get(VIEWER_UUID, ROOM_ID);

		assertEquals(ROOM_ID, result.getRoomId());
		assertEquals(PRODUCT_POST_UUID, result.getProductPost().getProductPostUuid());
		assertEquals("버버리 레더 포켓 미니 토트백", result.getProductPost().getProductPostName());
		assertEquals(1500000L, result.getProductPost().getPrice());
		assertEquals(TradeStatus.RESERVED, result.getProductPost().getTradeStatus());
		assertEquals(SELLER_UUID, result.getSeller().getMemberUuid());
		assertEquals("숭남농홍길동", result.getSeller().getNickname());
		assertEquals(SELLER_UUID, result.getCounterpart().getMemberUuid());
		assertEquals("숭남농홍길동", result.getCounterpart().getNickname());
		assertEquals("https://cdn.example.com/profiles/333.png", result.getCounterpart().getProfileImageUrl());
	}

	@Test
	void get_keepsProductUuidWhenSnapshotIsMissing() {
		roomStore.add(room());

		ChatRoomDetailResultDto result = service.get(VIEWER_UUID, ROOM_ID);

		assertEquals(PRODUCT_POST_UUID, result.getProductPost().getProductPostUuid());
		assertNull(result.getProductPost().getProductPostName());
		assertEquals("숭남농홍길동", result.getSeller().getNickname());
		assertEquals("숭남농홍길동", result.getCounterpart().getNickname());
	}

	@Test
	void get_returnsSameSellerWhenViewerIsSeller() {
		roomStore.add(room());

		ChatRoomDetailResultDto result = service.get(SELLER_UUID, ROOM_ID);

		assertEquals(SELLER_UUID, result.getSeller().getMemberUuid());
		assertEquals("숭남농홍길동", result.getSeller().getNickname());
		assertEquals(VIEWER_UUID, result.getCounterpart().getMemberUuid());
		assertEquals("중앙동홍길동", result.getCounterpart().getNickname());
		assertEquals("https://cdn.example.com/profiles/222.png", result.getCounterpart().getProfileImageUrl());
	}

	@Test
	void get_marksViewerReadAndPushesZeroUnread() {
		roomStore.add(room());
		Instant createdAt = Instant.parse("2026-08-20T05:00:00Z");
		messageStore.add(ChatMessage.restore(
				"msg-1",
				ROOM_ID,
				SELLER_UUID,
				MessageType.TEXT,
				"안녕하세요",
				null,
				createdAt
		));

		service.get(VIEWER_UUID, ROOM_ID);

		assertEquals(createdAt, lastReadStore.lastReadAt(ROOM_ID, VIEWER_UUID));
		assertNull(lastReadStore.lastReadAt(ROOM_ID, SELLER_UUID));
		assertEquals(0, listPublisher.preview(VIEWER_UUID).getUnreadCount());
		assertEquals(ROOM_ID, listPublisher.preview(VIEWER_UUID).getRoomId());
	}

	@Test
	void get_doesNotMarkReadWhenAccessDenied() {
		roomStore.add(room());

		assertThrows(ChatRoomAccessDeniedException.class, () -> service.get(STRANGER_UUID, ROOM_ID));
		assertNull(lastReadStore.lastReadAt(ROOM_ID, STRANGER_UUID));
		assertEquals(0, listPublisher.published.size());
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
			return messages.values().stream()
					.filter(message -> roomId.equals(message.getRoomId()))
					.sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
					.limit(limit)
					.toList();
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

	private static final class RecordingLastReadStore implements UpdateParticipantLastReadPort {

		private final Map<String, Instant> lastReads = new HashMap<>();

		Instant lastReadAt(String roomId, String memberUuid) {
			return lastReads.get(roomId + "|" + memberUuid);
		}

		@Override
		public void updateLastRead(String roomId, String memberUuid, Instant lastReadAt) {
			lastReads.put(roomId + "|" + memberUuid, lastReadAt);
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

		private static final class Published {
			private final String memberUuid;
			private final ChatListPreviewDto preview;

			private Published(String memberUuid, ChatListPreviewDto preview) {
				this.memberUuid = memberUuid;
				this.preview = preview;
			}
		}
	}

	private static final class InMemoryChatProductPostStore implements LoadChatProductPostPort {

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
	}

	private static final class StubResolveChatUserProfileUseCase implements ResolveChatUserProfileUseCase {

		@Override
		public ChatUserProfile resolve(String memberUuid) {
			if (VIEWER_UUID.equals(memberUuid)) {
				return ChatUserProfile.create(
						memberUuid,
						"중앙동홍길동",
						"https://cdn.example.com/profiles/222.png",
						MemberGrade.BRONZE
				);
			}
			return ChatUserProfile.create(
					memberUuid,
					"숭남농홍길동",
					"https://cdn.example.com/profiles/333.png",
					MemberGrade.GOLD
			);
		}
	}
}
