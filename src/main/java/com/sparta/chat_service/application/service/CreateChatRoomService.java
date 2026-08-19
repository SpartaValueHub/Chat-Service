package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.CreateChatRoomUseCase;
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
import com.sparta.chat_service.domain.model.Participant;
import com.sparta.chat_service.domain.model.TradeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateChatRoomService implements CreateChatRoomUseCase {

	// 기존 방 조회
	private final LoadChatRoomPort loadChatRoomPort;
	// 신규 방 저장
	private final SaveChatRoomPort saveChatRoomPort;
	// 참여자 프로필 적재 (Member)
	private final ResolveChatUserProfileUseCase resolveChatUserProfileUseCase;
	// 판매자 닉네임·등급 스냅샷 저장
	private final SaveChatUserProfilePort saveChatUserProfilePort;
	// 상품 게시글 스냅샷 저장
	private final SaveChatProductPostPort saveChatProductPostPort;

	@Override
	@Transactional
	public CreateChatRoomResultDto create(CreateChatRoomCommandDto command) {
		if (command == null) {
			throw new InvalidChatRoomRequestException("요청 본문이 필요합니다.");
		}

		String buyerUuid = requireMemberUuid(command.getBuyerUuid(), true);
		String productPostUuid = requireText(command.getProductPostUuid(), "productPostUuid는 필수입니다.");
		String sellerUuid = requireMemberUuid(command.getSellerUuid(), false);
		String productPostImageUrl = requireText(command.getProductPostImageUrl(), "productPostImageUrl은 필수입니다.");
		String productPostName = requireText(command.getProductPostName(), "productPostName은 필수입니다.");
		Long price = requirePrice(command.getPrice());
		TradeStatus tradeStatus = requireTradeStatus(command.getTradeStatus());
		String sellerNickname = requireText(command.getSellerNickname(), "sellerNickname은 필수입니다.");
		MemberGrade sellerMemberGrade = requireMemberGrade(command.getSellerMemberGrade());

		if (buyerUuid.equals(sellerUuid)) {
			throw new CannotChatWithSelfException();
		}

		resolveChatUserProfileUseCase.resolve(buyerUuid);
		ChatUserProfile sellerProfile = resolveChatUserProfileUseCase.resolve(sellerUuid);
		saveChatUserProfilePort.save(sellerProfile.applySellerSnapshot(sellerNickname, sellerMemberGrade));
		saveChatProductPostPort.save(ChatProductPost.create(
				productPostUuid,
				productPostImageUrl,
				productPostName,
				price,
				tradeStatus
		));

		return loadChatRoomPort.findByProductPostAndMembers(productPostUuid, buyerUuid, sellerUuid)
				.map(existing -> toResult(existing, buyerUuid, sellerUuid, true))
				.orElseGet(() -> toResult(saveNewRoom(productPostUuid, buyerUuid, sellerUuid), buyerUuid, sellerUuid, false));
	}

	private ChatRoom saveNewRoom(String productPostUuid, String buyerUuid, String sellerUuid) {
		Instant now = Instant.now();
		List<Participant> participants = List.of(
				Participant.join(buyerUuid, now),
				Participant.join(sellerUuid, now)
		);
		return saveChatRoomPort.save(ChatRoom.create(productPostUuid, participants));
	}

	private CreateChatRoomResultDto toResult(
			ChatRoom chatRoom,
			String buyerUuid,
			String sellerUuid,
			boolean reused
	) {
		return CreateChatRoomResultDto.builder()
				.roomId(chatRoom.getId())
				.productPostUuid(chatRoom.getProductPostUuid())
				.buyerUuid(buyerUuid)
				.sellerUuid(sellerUuid)
				.reused(reused)
				.build();
	}

	private String requireMemberUuid(String value, boolean authHeader) {
		String normalized = value == null ? "" : value.trim();
		if (!normalized.isBlank()) {
			return normalized;
		}
		if (authHeader) {
			throw new ChatAuthMissingException();
		}
		throw new InvalidChatRoomRequestException("sellerUuid는 필수입니다.");
	}

	private String requireText(String value, String message) {
		String normalized = value == null ? "" : value.trim();
		if (normalized.isBlank()) {
			throw new InvalidChatRoomRequestException(message);
		}
		return normalized;
	}

	private Long requirePrice(Long price) {
		if (price == null) {
			throw new InvalidChatRoomRequestException("price는 필수입니다.");
		}
		return price;
	}

	private TradeStatus requireTradeStatus(TradeStatus tradeStatus) {
		if (tradeStatus == null) {
			throw new InvalidChatRoomRequestException("tradeStatus는 필수입니다.");
		}
		return tradeStatus;
	}

	private MemberGrade requireMemberGrade(MemberGrade memberGrade) {
		if (memberGrade == null) {
			throw new InvalidChatRoomRequestException("sellerMemberGrade는 필수입니다.");
		}
		return memberGrade;
	}
}
