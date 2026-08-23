package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.ListChatRoomsUseCase;
import com.sparta.chat_service.application.port.in.dto.ChatRoomListCounterpartDto;
import com.sparta.chat_service.application.port.in.dto.ChatRoomListItemDto;
import com.sparta.chat_service.application.port.in.dto.ChatRoomListLastMessageDto;
import com.sparta.chat_service.application.port.in.dto.ChatRoomListProductDto;
import com.sparta.chat_service.application.port.in.dto.ChatRoomListResultDto;
import com.sparta.chat_service.application.port.out.LoadChatMessagePort;
import com.sparta.chat_service.application.port.out.LoadChatProductPostPort;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.domain.exception.ChatAuthMissingException;
import com.sparta.chat_service.domain.exception.InvalidChatRoomRequestException;
import com.sparta.chat_service.domain.model.ChatProductPost;
import com.sparta.chat_service.domain.model.ChatRoom;
import com.sparta.chat_service.domain.model.LastMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListChatRoomsService implements ListChatRoomsUseCase {

	private final LoadChatRoomPort loadChatRoomPort;
	private final LoadChatProductPostPort loadChatProductPostPort;
	private final LoadChatMessagePort loadChatMessagePort;

	@Override
	public ChatRoomListResultDto list(String memberUuid) {
		String viewerUuid = requireMemberUuid(memberUuid);
		return toResult(viewerUuid, loadChatRoomPort.findByParticipant(viewerUuid));
	}

	@Override
	public ChatRoomListResultDto listByProductPost(String memberUuid, String productPostUuid) {
		String viewerUuid = requireMemberUuid(memberUuid);
		String normalizedProductPostUuid = requireProductPostUuid(productPostUuid);
		return toResult(
				viewerUuid,
				loadChatRoomPort.findByParticipantAndProductPost(viewerUuid, normalizedProductPostUuid)
		);
	}

	private ChatRoomListResultDto toResult(String viewerUuid, List<ChatRoom> rooms) {
		List<ChatRoom> sorted = sortForList(rooms);
		Map<String, ChatProductPost> productPosts = loadProductPosts(sorted);
		List<ChatRoomListItemDto> items = sorted.stream()
				.map(room -> toItem(room, viewerUuid, productPosts))
				.toList();
		return ChatRoomListResultDto.builder()
				.rooms(items)
				.build();
	}

	private Map<String, ChatProductPost> loadProductPosts(List<ChatRoom> rooms) {
		List<String> productPostUuids = rooms.stream()
				.map(ChatRoom::getProductPostUuid)
				.filter(uuid -> uuid != null && !uuid.isBlank())
				.distinct()
				.toList();
		return loadChatProductPostPort.findAllByProductPostUuids(productPostUuids).stream()
				.collect(Collectors.toMap(ChatProductPost::getProductPostUuid, Function.identity(), (left, right) -> left));
	}

	private ChatRoomListItemDto toItem(
			ChatRoom room,
			String viewerUuid,
			Map<String, ChatProductPost> productPosts
	) {
		return ChatRoomListItemDto.builder()
				.roomId(room.getId())
				.productPost(toProduct(room.getProductPostUuid(), productPosts.get(room.getProductPostUuid())))
				.counterpart(ChatRoomListCounterpartDto.builder()
						.memberUuid(room.counterpartUuid(viewerUuid).orElse(null))
						.build())
				.lastMessage(toLastMessage(room.getLastMessage()))
				.unreadCount(loadChatMessagePort.countUnread(
						room.getId(),
						viewerUuid,
						room.lastReadAt(viewerUuid).orElse(null)
				))
				.updatedAt(room.getUpdatedAt())
				.build();
	}

	private ChatRoomListProductDto toProduct(String productPostUuid, ChatProductPost productPost) {
		if (productPost == null) {
			return ChatRoomListProductDto.builder()
					.productPostUuid(productPostUuid)
					.build();
		}
		return ChatRoomListProductDto.builder()
				.productPostUuid(productPost.getProductPostUuid())
				.productPostImageUrl(productPost.getProductPostImageUrl())
				.productPostName(productPost.getProductPostName())
				.price(productPost.getPrice())
				.tradeStatus(productPost.getTradeStatus())
				.build();
	}

	private ChatRoomListLastMessageDto toLastMessage(LastMessage lastMessage) {
		if (lastMessage == null) {
			return null;
		}
		return ChatRoomListLastMessageDto.builder()
				.content(lastMessage.getContent())
				.createdAt(lastMessage.getCreatedAt())
				.build();
	}

	private List<ChatRoom> sortForList(List<ChatRoom> rooms) {
		return rooms.stream()
				.sorted(Comparator
						.comparing(this::lastMessageCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
						.thenComparing(ChatRoom::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
				.toList();
	}

	private Instant lastMessageCreatedAt(ChatRoom room) {
		LastMessage lastMessage = room.getLastMessage();
		return lastMessage == null ? null : lastMessage.getCreatedAt();
	}

	private String requireMemberUuid(String memberUuid) {
		String normalized = memberUuid == null ? "" : memberUuid.trim();
		if (normalized.isBlank()) {
			throw new ChatAuthMissingException();
		}
		return normalized;
	}

	private String requireProductPostUuid(String productPostUuid) {
		String normalized = productPostUuid == null ? "" : productPostUuid.trim();
		if (normalized.isBlank()) {
			throw new InvalidChatRoomRequestException("productPostUuid는 필수입니다.");
		}
		return normalized;
	}
}
