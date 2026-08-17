package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.out.LoadChatUserProfilePort;
import com.sparta.chat_service.application.port.out.LoadMemberProfilePort;
import com.sparta.chat_service.application.port.out.SaveChatUserProfilePort;
import com.sparta.chat_service.domain.exception.ChatUserProfileNotFoundException;
import com.sparta.chat_service.domain.exception.InvalidMemberUuidException;
import com.sparta.chat_service.domain.model.ChatUserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResolveChatUserProfileServiceTest {

	private static final String MEMBER_UUID = "550e8400-e29b-41d4-a716-446655440000";

	// 로컬 Read Model Fake
	private InMemoryChatUserProfileStore localStore;
	// Member 공개 프로필 Fake
	private InMemoryMemberProfileStore memberStore;
	private ResolveChatUserProfileService service;

	@BeforeEach
	void setUp() {
		localStore = new InMemoryChatUserProfileStore();
		memberStore = new InMemoryMemberProfileStore();
		service = new ResolveChatUserProfileService(localStore, localStore, memberStore);
	}

	@Test
	void resolve_returnsLocalProfileWithoutCallingMember() {
		localStore.save(ChatUserProfile.create(MEMBER_UUID, "로컬닉", "https://img.local/a.png"));
		memberStore.put(ChatUserProfile.create(MEMBER_UUID, "멤버닉", "https://img.member/a.png"));

		ChatUserProfile result = service.resolve(MEMBER_UUID);

		assertEquals("로컬닉", result.getNickname());
		assertEquals(0, memberStore.callCount);
	}

	@Test
	void resolve_fetchesFromMemberAndSavesWhenLocalMissing() {
		memberStore.put(ChatUserProfile.create(MEMBER_UUID, "홍길동", "https://img.member/a.png"));

		ChatUserProfile result = service.resolve("  " + MEMBER_UUID + "  ");

		assertEquals("홍길동", result.getNickname());
		assertEquals("https://img.member/a.png", result.getProfileImageUrl());
		assertTrue(localStore.findByMemberUuid(MEMBER_UUID).isPresent());
		assertEquals("홍길동", localStore.findByMemberUuid(MEMBER_UUID).get().getNickname());
	}

	@Test
	void resolve_throwsWhenMemberMissing() {
		assertThrows(ChatUserProfileNotFoundException.class, () -> service.resolve(MEMBER_UUID));
	}

	@Test
	void resolve_throwsWhenMemberUuidBlank() {
		assertThrows(InvalidMemberUuidException.class, () -> service.resolve("  "));
		assertThrows(InvalidMemberUuidException.class, () -> service.resolve(null));
	}

	private static final class InMemoryChatUserProfileStore
			implements LoadChatUserProfilePort, SaveChatUserProfilePort {

		// memberUuid -> 프로필
		private final Map<String, ChatUserProfile> profiles = new HashMap<>();

		@Override
		public Optional<ChatUserProfile> findByMemberUuid(String memberUuid) {
			return Optional.ofNullable(profiles.get(memberUuid));
		}

		@Override
		public ChatUserProfile save(ChatUserProfile profile) {
			profiles.put(profile.getMemberUuid(), profile);
			return profile;
		}
	}

	private static final class InMemoryMemberProfileStore implements LoadMemberProfilePort {

		// memberUuid -> 프로필
		private final Map<String, ChatUserProfile> profiles = new HashMap<>();
		// Member 조회 호출 횟수
		private int callCount;

		void put(ChatUserProfile profile) {
			profiles.put(profile.getMemberUuid(), profile);
		}

		@Override
		public Optional<ChatUserProfile> findByMemberUuid(String memberUuid) {
			callCount++;
			return Optional.ofNullable(profiles.get(memberUuid));
		}
	}
}
