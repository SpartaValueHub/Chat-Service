package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.dto.IssueChatImageUploadUrlCommandDto;
import com.sparta.chat_service.application.port.in.dto.IssueChatImageUploadUrlResultDto;
import com.sparta.chat_service.application.port.out.IssuePresignedUploadPort;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.application.port.out.dto.PresignedUploadDto;
import com.sparta.chat_service.config.ChatMediaProperties;
import com.sparta.chat_service.domain.exception.ChatAuthMissingException;
import com.sparta.chat_service.domain.exception.ChatRoomAccessDeniedException;
import com.sparta.chat_service.domain.exception.ChatRoomNotFoundException;
import com.sparta.chat_service.domain.exception.InvalidChatRoomRequestException;
import com.sparta.chat_service.domain.model.ChatRoom;
import com.sparta.chat_service.domain.model.ChatRoomStatus;
import com.sparta.chat_service.domain.model.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IssueChatImageUploadUrlServiceTest {

	private static final String MEMBER_UUID = "22222222-2222-4222-8222-222222222222";
	private static final String SELLER_UUID = "33333333-3333-4333-8333-333333333333";
	private static final String STRANGER_UUID = "99999999-9999-4999-8999-999999999999";
	private static final String PRODUCT_POST_UUID = "11111111-1111-4111-8111-111111111111";
	private static final String ROOM_ID = "room-1";

	private InMemoryChatRoomStore roomStore;
	private IssueChatImageUploadUrlService service;

	@BeforeEach
	void setUp() {
		roomStore = new InMemoryChatRoomStore();
		ChatMediaProperties properties = new ChatMediaProperties();
		properties.setCloudfrontBaseUrl("https://cdn.example.com");
		properties.setMaxFileSizeBytes(5_242_880L);
		properties.setPresignExpireSeconds(300);
		service = new IssueChatImageUploadUrlService(
				roomStore,
				new StubPresigner(),
				new ChatImageUrlResolver(properties.getCloudfrontBaseUrl()),
				properties
		);
		roomStore.add(room());
	}

	@Test
	void issue_rejectsMissingMember() {
		assertThrows(ChatAuthMissingException.class, () -> service.issue(command("  ", "image/jpeg", 1024L)));
	}

	@Test
	void issue_rejectsUnknownRoom() {
		IssueChatImageUploadUrlCommandDto command = IssueChatImageUploadUrlCommandDto.builder()
				.memberUuid(MEMBER_UUID)
				.roomId("missing")
				.contentType("image/jpeg")
				.fileSize(1024L)
				.build();
		assertThrows(ChatRoomNotFoundException.class, () -> service.issue(command));
	}

	@Test
	void issue_rejectsNonParticipant() {
		assertThrows(ChatRoomAccessDeniedException.class,
				() -> service.issue(command(STRANGER_UUID, "image/jpeg", 1024L)));
	}

	@Test
	void issue_rejectsUnsupportedContentType() {
		assertThrows(InvalidChatRoomRequestException.class,
				() -> service.issue(command(MEMBER_UUID, "image/svg+xml", 1024L)));
	}

	@Test
	void issue_rejectsOversizedFile() {
		assertThrows(InvalidChatRoomRequestException.class,
				() -> service.issue(command(MEMBER_UUID, "image/jpeg", 5_242_881L)));
	}

	@Test
	void issue_rejectsMissingFileSize() {
		assertThrows(InvalidChatRoomRequestException.class,
				() -> service.issue(command(MEMBER_UUID, "image/png", null)));
	}

	@Test
	void issue_returnsPresignedPutForJpeg() {
		IssueChatImageUploadUrlResultDto result = service.issue(command(MEMBER_UUID, "image/jpeg", 2_457_600L));

		assertEquals("PUT", result.getMethod());
		assertEquals("image/jpeg", result.getContentType());
		assertEquals(300, result.getExpiresInSeconds());
		assertTrue(result.getS3Key().startsWith("chat/"));
		assertTrue(result.getS3Key().endsWith(".jpg"));
		assertEquals("https://cdn.example.com/" + result.getS3Key(), result.getPublicUrl());
		assertEquals("https://s3.example.com/" + result.getS3Key(), result.getUploadUrl());
	}

	private IssueChatImageUploadUrlCommandDto command(String memberUuid, String contentType, Long fileSize) {
		return IssueChatImageUploadUrlCommandDto.builder()
				.memberUuid(memberUuid)
				.roomId(ROOM_ID)
				.contentType(contentType)
				.fileSize(fileSize)
				.build();
	}

	private ChatRoom room() {
		Instant joinedAt = Instant.parse("2026-08-01T00:00:00Z");
		return ChatRoom.restore(
				ROOM_ID,
				PRODUCT_POST_UUID,
				SELLER_UUID,
				List.of(Participant.join(MEMBER_UUID, joinedAt), Participant.join(SELLER_UUID, joinedAt)),
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

		@Override
		public List<ChatRoom> findByParticipantAndProductPost(String memberUuid, String productPostUuid) {
			return List.of();
		}
	}

	private static final class StubPresigner implements IssuePresignedUploadPort {

		@Override
		public PresignedUploadDto issuePutUrl(String s3Key, String contentType, int expiresInSeconds) {
			return PresignedUploadDto.builder()
					.uploadUrl("https://s3.example.com/" + s3Key)
					.s3Key(s3Key)
					.expiresInSeconds(expiresInSeconds)
					.build();
		}
	}
}
