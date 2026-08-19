package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.ResolveChatUserProfileUseCase;
import com.sparta.chat_service.application.port.in.dto.CreateChatRoomCommandDto;
import com.sparta.chat_service.application.port.in.dto.CreateChatRoomResultDto;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.application.port.out.SaveChatProductPostPort;
import com.sparta.chat_service.application.port.out.SaveChatRoomPort;
import com.sparta.chat_service.application.port.out.SaveChatUserProfilePort;
import com.sparta.chat_service.domain.exception.CannotChatWithSelfException;
import com.sparta.chat_service.domain.exception.ChatAuthMissingException;
import com.sparta.chat_service.domain.exception.InvalidChatRoomRequestException;
import com.sparta.chat_service.domain.model.ChatProductPost;
import com.sparta.chat_service.domain.model.ChatRoom;
import com.sparta.chat_service.domain.model.ChatUserProfile;
import com.sparta.chat_service.domain.model.MemberGrade;
import com.sparta.chat_service.domain.model.TradeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateChatRoomServiceTest {

	private static final String BUYER_UUID = "22222222-2222-4222-8222-222222222222";
	private static final String SELLER_UUID = "33333333-3333-4333-8333-333333333333";
	private static final String PRODUCT_POST_UUID = "11111111-1111-4111-8111-111111111111";

	private InMemoryChatRoomStore roomStore;
	private StubResolveChatUserProfileUseCase resolveUseCase;
	private InMemoryChatUserProfileStore profileStore;
	private InMemoryChatProductPostStore productPostStore;
	private CreateChatRoomService service;

	@BeforeEach
	void setUp() {
		roomStore = new InMemoryChatRoomStore();
		resolveUseCase = new StubResolveChatUserProfileUseCase();
		profileStore = new InMemoryChatUserProfileStore();
		productPostStore = new InMemoryChatProductPostStore();
		service = new CreateChatRoomService(
				roomStore,
				roomStore,
				resolveUseCase,
				profileStore,
				productPostStore
		);
	}

	@Test
	void create_savesNewRoomAndUpsertsReadModels() {
		CreateChatRoomResultDto result = service.create(command(BUYER_UUID, PRODUCT_POST_UUID, SELLER_UUID));

		assertFalse(result.isReused());
		assertEquals(PRODUCT_POST_UUID, result.getProductPostUuid());
		assertEquals(BUYER_UUID, result.getBuyerUuid());
		assertEquals(SELLER_UUID, result.getSellerUuid());
		assertEquals(1, roomStore.saveCount.get());
		assertEquals(2, resolveUseCase.callCount.get());

		ChatProductPost productPost = productPostStore.posts.get(PRODUCT_POST_UUID);
		assertEquals("중고 노트북", productPost.getProductPostName());
		assertEquals(350000L, productPost.getPrice());
		assertEquals(TradeStatus.SELLING, productPost.getTradeStatus());

		ChatUserProfile seller = profileStore.profiles.get(SELLER_UUID);
		assertEquals("판매자닉", seller.getNickname());
		assertEquals(MemberGrade.GOLD, seller.getMemberGrade());
		assertEquals("/images/default-profile.png", seller.getProfileImageUrl());
	}

	@Test
	void create_reusesExistingRoomForSamePair() {
		service.create(command(BUYER_UUID, PRODUCT_POST_UUID, SELLER_UUID));

		CreateChatRoomResultDto result = service.create(
				command("  " + BUYER_UUID + "  ", PRODUCT_POST_UUID, SELLER_UUID)
		);

		assertTrue(result.isReused());
		assertEquals(1, roomStore.saveCount.get());
		assertEquals(2, productPostStore.saveCount.get());
	}

	@Test
	void create_reusesWhenBuyerAndSellerOrderDiffersInLookup() {
		service.create(command(BUYER_UUID, PRODUCT_POST_UUID, SELLER_UUID));

		CreateChatRoomResultDto result = service.create(command(SELLER_UUID, PRODUCT_POST_UUID, BUYER_UUID));

		assertTrue(result.isReused());
		assertEquals(SELLER_UUID, result.getBuyerUuid());
		assertEquals(BUYER_UUID, result.getSellerUuid());
		assertEquals(1, roomStore.saveCount.get());
	}

	@Test
	void create_rejectsSelfChat() {
		assertThrows(CannotChatWithSelfException.class,
				() -> service.create(command(BUYER_UUID, PRODUCT_POST_UUID, BUYER_UUID)));
	}

	@Test
	void create_rejectsMissingBuyerHeader() {
		assertThrows(ChatAuthMissingException.class,
				() -> service.create(command("  ", PRODUCT_POST_UUID, SELLER_UUID)));
	}

	@Test
	void create_rejectsBlankProductPostUuid() {
		assertThrows(InvalidChatRoomRequestException.class,
				() -> service.create(command(BUYER_UUID, " ", SELLER_UUID)));
	}

	@Test
	void create_rejectsMissingPrice() {
		CreateChatRoomCommandDto command = CreateChatRoomCommandDto.builder()
				.buyerUuid(BUYER_UUID)
				.productPostUuid(PRODUCT_POST_UUID)
				.sellerUuid(SELLER_UUID)
				.productPostImageUrl("https://cdn.example.com/products/111.png")
				.productPostName("중고 노트북")
				.price(null)
				.tradeStatus(TradeStatus.SELLING)
				.sellerNickname("판매자닉")
				.sellerMemberGrade(MemberGrade.GOLD)
				.build();

		assertThrows(InvalidChatRoomRequestException.class, () -> service.create(command));
	}

	private CreateChatRoomCommandDto command(String buyerUuid, String productPostUuid, String sellerUuid) {
		return CreateChatRoomCommandDto.builder()
				.buyerUuid(buyerUuid)
				.productPostUuid(productPostUuid)
				.sellerUuid(sellerUuid)
				.productPostImageUrl("https://cdn.example.com/products/111.png")
				.productPostName("중고 노트북")
				.price(350000L)
				.tradeStatus(TradeStatus.SELLING)
				.sellerNickname("판매자닉")
				.sellerMemberGrade(MemberGrade.GOLD)
				.build();
	}

	private static final class InMemoryChatRoomStore implements LoadChatRoomPort, SaveChatRoomPort {

		// 저장된 방
		private final Map<String, ChatRoom> rooms = new HashMap<>();
		// save 호출 횟수
		private final AtomicInteger saveCount = new AtomicInteger();

		@Override
		public Optional<ChatRoom> findByProductPostAndMembers(
				String productPostUuid,
				String memberUuid1,
				String memberUuid2
		) {
			return rooms.values().stream()
					.filter(room -> productPostUuid.equals(room.getProductPostUuid()))
					.filter(room -> room.getParticipants().size() == 2)
					.filter(room -> hasMember(room, memberUuid1) && hasMember(room, memberUuid2))
					.findFirst();
		}

		@Override
		public List<ChatRoom> findByParticipant(String memberUuid) {
			return rooms.values().stream()
					.filter(room -> hasMember(room, memberUuid))
					.toList();
		}

		@Override
		public ChatRoom save(ChatRoom chatRoom) {
			saveCount.incrementAndGet();
			ChatRoom stored = ChatRoom.restore(
					"room-" + saveCount.get(),
					chatRoom.getProductPostUuid(),
					chatRoom.getParticipants(),
					chatRoom.getLastMessage(),
					chatRoom.getStatus(),
					chatRoom.getCreatedAt(),
					chatRoom.getUpdatedAt()
			);
			rooms.put(stored.getId(), stored);
			return stored;
		}

		private boolean hasMember(ChatRoom room, String memberUuid) {
			return room.getParticipants().stream()
					.anyMatch(participant -> memberUuid.equals(participant.getMemberUuid()));
		}
	}

	private static final class StubResolveChatUserProfileUseCase implements ResolveChatUserProfileUseCase {

		// resolve 호출 횟수
		private final AtomicInteger callCount = new AtomicInteger();

		@Override
		public ChatUserProfile resolve(String memberUuid) {
			callCount.incrementAndGet();
			String nickname = BUYER_UUID.equals(memberUuid) ? "구매자" : "판매자";
			return ChatUserProfile.create(memberUuid, nickname, "/images/default-profile.png");
		}
	}

	private static final class InMemoryChatUserProfileStore implements SaveChatUserProfilePort {

		// memberUuid -> 프로필
		private final Map<String, ChatUserProfile> profiles = new HashMap<>();

		@Override
		public ChatUserProfile save(ChatUserProfile profile) {
			profiles.put(profile.getMemberUuid(), profile);
			return profile;
		}
	}

	private static final class InMemoryChatProductPostStore implements SaveChatProductPostPort {

		// productPostUuid -> 상품 게시글스냅샷
		private final Map<String, ChatProductPost> posts = new HashMap<>();
		// save 호출 횟수
		private final AtomicInteger saveCount = new AtomicInteger();

		@Override
		public ChatProductPost save(ChatProductPost productPost) {
			saveCount.incrementAndGet();
			posts.put(productPost.getProductPostUuid(), productPost);
			return productPost;
		}
	}
}
