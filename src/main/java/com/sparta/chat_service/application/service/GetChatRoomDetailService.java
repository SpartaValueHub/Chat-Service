package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.GetChatRoomDetailUseCase;
import com.sparta.chat_service.application.port.in.ResolveChatUserProfileUseCase;
import com.sparta.chat_service.application.port.in.dto.ChatRoomDetailCounterpartDto;
import com.sparta.chat_service.application.port.in.dto.ChatRoomDetailProductDto;
import com.sparta.chat_service.application.port.in.dto.ChatRoomDetailResultDto;
import com.sparta.chat_service.application.port.in.dto.ChatRoomDetailSellerDto;
import com.sparta.chat_service.application.port.out.LoadChatProductPostPort;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.domain.exception.ChatAuthMissingException;
import com.sparta.chat_service.domain.exception.ChatRoomAccessDeniedException;
import com.sparta.chat_service.domain.exception.ChatRoomNotFoundException;
import com.sparta.chat_service.domain.exception.InvalidChatRoomRequestException;
import com.sparta.chat_service.domain.model.ChatProductPost;
import com.sparta.chat_service.domain.model.ChatRoom;
import com.sparta.chat_service.domain.model.ChatUserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetChatRoomDetailService implements GetChatRoomDetailUseCase {

	private final LoadChatRoomPort loadChatRoomPort;
	private final LoadChatProductPostPort loadChatProductPostPort;
	private final ResolveChatUserProfileUseCase resolveChatUserProfileUseCase;

	@Override
	public ChatRoomDetailResultDto get(String memberUuid, String roomId) {
		String viewerUuid = requireMemberUuid(memberUuid);
		ChatRoom room = requireAccessibleRoom(viewerUuid, roomId);
		Map<String, ChatUserProfile> resolvedProfiles = new HashMap<>();
		return ChatRoomDetailResultDto.builder()
				.roomId(room.getId())
				.productPost(toProduct(room.getProductPostUuid()))
				.seller(toSeller(room.getSellerUuid(), resolvedProfiles))
				.counterpart(toCounterpart(room.counterpartUuid(viewerUuid).orElse(null), resolvedProfiles))
				.build();
	}

	private ChatRoom requireAccessibleRoom(String viewerUuid, String roomId) {
		String normalizedRoomId = requireRoomId(roomId);
		ChatRoom room = loadChatRoomPort.findById(normalizedRoomId)
				.orElseThrow(ChatRoomNotFoundException::new);
		if (!room.hasParticipant(viewerUuid)) {
			throw new ChatRoomAccessDeniedException();
		}
		return room;
	}

	private ChatRoomDetailProductDto toProduct(String productPostUuid) {
		if (productPostUuid == null || productPostUuid.isBlank()) {
			return null;
		}
		ChatProductPost productPost = loadChatProductPostPort.findByProductPostUuid(productPostUuid)
				.orElse(null);
		if (productPost == null) {
			return ChatRoomDetailProductDto.builder()
					.productPostUuid(productPostUuid)
					.build();
		}
		return ChatRoomDetailProductDto.builder()
				.productPostUuid(productPost.getProductPostUuid())
				.productPostImageUrl(productPost.getProductPostImageUrl())
				.productPostName(productPost.getProductPostName())
				.price(productPost.getPrice())
				.tradeStatus(productPost.getTradeStatus())
				.build();
	}

	private ChatRoomDetailSellerDto toSeller(String sellerUuid, Map<String, ChatUserProfile> resolvedProfiles) {
		ChatUserProfile profile = resolveProfile(sellerUuid, resolvedProfiles);
		if (profile == null) {
			return null;
		}
		return ChatRoomDetailSellerDto.builder()
				.memberUuid(profile.getMemberUuid())
				.nickname(profile.getNickname())
				.build();
	}

	private ChatRoomDetailCounterpartDto toCounterpart(String counterpartUuid, Map<String, ChatUserProfile> resolvedProfiles) {
		ChatUserProfile profile = resolveProfile(counterpartUuid, resolvedProfiles);
		if (profile == null) {
			return null;
		}
		return ChatRoomDetailCounterpartDto.builder()
				.memberUuid(profile.getMemberUuid())
				.nickname(profile.getNickname())
				.profileImageUrl(profile.getProfileImageUrl())
				.build();
	}

	private ChatUserProfile resolveProfile(String memberUuid, Map<String, ChatUserProfile> resolvedProfiles) {
		if (memberUuid == null || memberUuid.isBlank()) {
			return null;
		}
		return resolvedProfiles.computeIfAbsent(memberUuid, resolveChatUserProfileUseCase::resolve);
	}

	private String requireMemberUuid(String memberUuid) {
		String normalized = memberUuid == null ? "" : memberUuid.trim();
		if (normalized.isBlank()) {
			throw new ChatAuthMissingException();
		}
		return normalized;
	}

	private String requireRoomId(String roomId) {
		String normalized = roomId == null ? "" : roomId.trim();
		if (normalized.isBlank()) {
			throw new InvalidChatRoomRequestException("roomId는 필수입니다.");
		}
		return normalized;
	}
}
