package com.sparta.chat_service.adaptor.in.web.controller;

import com.sparta.chat_service.adaptor.in.web.vo.ChatMessageListResponseVo;
import com.sparta.chat_service.adaptor.in.web.vo.ChatRoomDetailResponseVo;
import com.sparta.chat_service.adaptor.in.web.vo.ChatRoomListItemResponseVo;
import com.sparta.chat_service.adaptor.in.web.vo.ChatRoomListResponseVo;
import com.sparta.chat_service.adaptor.in.web.vo.CreateChatRoomRequestVo;
import com.sparta.chat_service.adaptor.in.web.vo.CreateChatRoomResponseVo;
import com.sparta.chat_service.adaptor.in.web.vo.TotalUnreadCountResponseVo;
import com.sparta.chat_service.application.port.in.CreateChatRoomUseCase;
import com.sparta.chat_service.application.port.in.GetChatRoomDetailUseCase;
import com.sparta.chat_service.application.port.in.GetTotalUnreadCountUseCase;
import com.sparta.chat_service.application.port.in.ListChatMessagesUseCase;
import com.sparta.chat_service.application.port.in.ListChatRoomsUseCase;
import com.sparta.chat_service.application.port.in.dto.ChatMessageItemDto;
import com.sparta.chat_service.application.port.in.dto.ChatMessageListResultDto;
import com.sparta.chat_service.application.port.in.dto.ChatMessageMetadataDto;
import com.sparta.chat_service.application.port.in.dto.ChatRoomDetailCounterpartDto;
import com.sparta.chat_service.application.port.in.dto.ChatRoomDetailProductDto;
import com.sparta.chat_service.application.port.in.dto.ChatRoomDetailResultDto;
import com.sparta.chat_service.application.port.in.dto.ChatRoomDetailSellerDto;
import com.sparta.chat_service.application.port.in.dto.ChatRoomListCounterpartDto;
import com.sparta.chat_service.application.port.in.dto.ChatRoomListItemDto;
import com.sparta.chat_service.application.port.in.dto.ChatRoomListLastMessageDto;
import com.sparta.chat_service.application.port.in.dto.ChatRoomListProductDto;
import com.sparta.chat_service.application.port.in.dto.ChatRoomListResultDto;
import com.sparta.chat_service.application.port.in.dto.CreateChatRoomCommandDto;
import com.sparta.chat_service.application.port.in.dto.CreateChatRoomResultDto;
import com.sparta.chat_service.domain.exception.InvalidChatRoomRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@RestController
public class ChatRoomController {

	// Gateway가 주입하는 회원 UUID 헤더
	private static final String MEMBER_UUID_HEADER = "X-Member-Uuid";

	private final CreateChatRoomUseCase createChatRoomUseCase;
	private final ListChatRoomsUseCase listChatRoomsUseCase;
	private final GetTotalUnreadCountUseCase getTotalUnreadCountUseCase;
	private final GetChatRoomDetailUseCase getChatRoomDetailUseCase;
	private final ListChatMessagesUseCase listChatMessagesUseCase;

	@GetMapping("/rooms")
	public ResponseEntity<ChatRoomListResponseVo> listChatRooms(
			@RequestHeader(value = MEMBER_UUID_HEADER, required = false) String memberUuid
	) {
		ChatRoomListResultDto resultDto = listChatRoomsUseCase.list(memberUuid);
		return ResponseEntity.ok(toListVo(resultDto));
	}

	@GetMapping("/unread-count")
	public ResponseEntity<TotalUnreadCountResponseVo> getTotalUnreadCount(
			@RequestHeader(value = MEMBER_UUID_HEADER, required = false) String memberUuid
	) {
		return ResponseEntity.ok(TotalUnreadCountResponseVo.builder()
				.totalUnreadCount(getTotalUnreadCountUseCase.get(memberUuid).getTotalUnreadCount())
				.build());
	}

	@GetMapping("/rooms/{roomId}")
	public ResponseEntity<ChatRoomDetailResponseVo> getChatRoom(
			@RequestHeader(value = MEMBER_UUID_HEADER, required = false) String memberUuid,
			@PathVariable String roomId
	) {
		ChatRoomDetailResultDto resultDto = getChatRoomDetailUseCase.get(memberUuid, roomId);
		return ResponseEntity.ok(toDetailVo(resultDto));
	}

	@GetMapping("/rooms/{roomId}/messages")
	public ResponseEntity<ChatMessageListResponseVo> listChatMessages(
			@RequestHeader(value = MEMBER_UUID_HEADER, required = false) String memberUuid,
			@PathVariable String roomId,
			@RequestParam(value = "before", required = false) String before,
			@RequestParam(value = "limit", required = false) Integer limit
	) {
		ChatMessageListResultDto resultDto = listChatMessagesUseCase.list(memberUuid, roomId, before, limit);
		return ResponseEntity.ok(toMessageListVo(resultDto));
	}

	@PostMapping("/rooms")
	public ResponseEntity<CreateChatRoomResponseVo> createChatRoom(
			@RequestHeader(value = MEMBER_UUID_HEADER, required = false) String buyerUuid,
			@RequestBody CreateChatRoomRequestVo requestVo
	) {
		if (requestVo == null) {
			throw new InvalidChatRoomRequestException("요청 본문이 필요합니다.");
		}
		CreateChatRoomResultDto resultDto = createChatRoomUseCase.create(toCommand(buyerUuid, requestVo));
		CreateChatRoomResponseVo responseVo = toVo(resultDto);
		HttpStatus status = resultDto.isReused() ? HttpStatus.OK : HttpStatus.CREATED;
		return ResponseEntity.status(status).body(responseVo);
	}

	private CreateChatRoomCommandDto toCommand(String buyerUuid, CreateChatRoomRequestVo requestVo) {
		return CreateChatRoomCommandDto.builder()
				.buyerUuid(buyerUuid)
				.productPostUuid(requestVo.getProductPostUuid())
				.sellerUuid(requestVo.getSellerUuid())
				.productPostImageUrl(requestVo.getProductPostImageUrl())
				.productPostName(requestVo.getProductPostName())
				.price(requestVo.getPrice())
				.tradeStatus(requestVo.getTradeStatus())
				.sellerNickname(requestVo.getSellerNickname())
				.sellerMemberGrade(requestVo.getSellerMemberGrade())
				.build();
	}

	private CreateChatRoomResponseVo toVo(CreateChatRoomResultDto resultDto) {
		return CreateChatRoomResponseVo.builder()
				.roomId(resultDto.getRoomId())
				.productPostUuid(resultDto.getProductPostUuid())
				.buyerUuid(resultDto.getBuyerUuid())
				.sellerUuid(resultDto.getSellerUuid())
				.reused(resultDto.isReused())
				.build();
	}

	private ChatRoomListResponseVo toListVo(ChatRoomListResultDto resultDto) {
		return ChatRoomListResponseVo.builder()
				.rooms(resultDto.getRooms().stream().map(this::toItemVo).toList())
				.build();
	}

	private ChatRoomListItemResponseVo toItemVo(ChatRoomListItemDto itemDto) {
		return ChatRoomListItemResponseVo.builder()
				.roomId(itemDto.getRoomId())
				.productPost(toProductVo(itemDto.getProductPost()))
				.counterpart(toCounterpartVo(itemDto.getCounterpart()))
				.lastMessage(toLastMessageVo(itemDto.getLastMessage()))
				.unreadCount(itemDto.getUnreadCount())
				.updatedAt(itemDto.getUpdatedAt())
				.build();
	}

	private ChatRoomListItemResponseVo.ProductPost toProductVo(ChatRoomListProductDto productDto) {
		if (productDto == null) {
			return null;
		}
		return ChatRoomListItemResponseVo.ProductPost.builder()
				.productPostUuid(productDto.getProductPostUuid())
				.productPostImageUrl(productDto.getProductPostImageUrl())
				.productPostName(productDto.getProductPostName())
				.price(productDto.getPrice())
				.tradeStatus(productDto.getTradeStatus())
				.build();
	}

	private ChatRoomListItemResponseVo.Counterpart toCounterpartVo(ChatRoomListCounterpartDto counterpartDto) {
		if (counterpartDto == null) {
			return null;
		}
		return ChatRoomListItemResponseVo.Counterpart.builder()
				.memberUuid(counterpartDto.getMemberUuid())
				.build();
	}

	private ChatRoomListItemResponseVo.LastMessage toLastMessageVo(ChatRoomListLastMessageDto lastMessageDto) {
		if (lastMessageDto == null) {
			return null;
		}
		return ChatRoomListItemResponseVo.LastMessage.builder()
				.content(lastMessageDto.getContent())
				.createdAt(lastMessageDto.getCreatedAt())
				.build();
	}

	private ChatRoomDetailResponseVo toDetailVo(ChatRoomDetailResultDto resultDto) {
		return ChatRoomDetailResponseVo.builder()
				.roomId(resultDto.getRoomId())
				.productPost(toDetailProductVo(resultDto.getProductPost()))
				.seller(toSellerVo(resultDto.getSeller()))
				.counterpart(toCounterpartVo(resultDto.getCounterpart()))
				.build();
	}

	private ChatRoomDetailResponseVo.ProductPost toDetailProductVo(ChatRoomDetailProductDto productDto) {
		if (productDto == null) {
			return null;
		}
		return ChatRoomDetailResponseVo.ProductPost.builder()
				.productPostUuid(productDto.getProductPostUuid())
				.productPostImageUrl(productDto.getProductPostImageUrl())
				.productPostName(productDto.getProductPostName())
				.price(productDto.getPrice())
				.tradeStatus(productDto.getTradeStatus())
				.build();
	}

	private ChatRoomDetailResponseVo.Seller toSellerVo(ChatRoomDetailSellerDto sellerDto) {
		if (sellerDto == null) {
			return null;
		}
		return ChatRoomDetailResponseVo.Seller.builder()
				.memberUuid(sellerDto.getMemberUuid())
				.nickname(sellerDto.getNickname())
				.build();
	}

	private ChatRoomDetailResponseVo.Counterpart toCounterpartVo(ChatRoomDetailCounterpartDto counterpartDto) {
		if (counterpartDto == null) {
			return null;
		}
		return ChatRoomDetailResponseVo.Counterpart.builder()
				.memberUuid(counterpartDto.getMemberUuid())
				.nickname(counterpartDto.getNickname())
				.profileImageUrl(counterpartDto.getProfileImageUrl())
				.build();
	}

	private ChatMessageListResponseVo toMessageListVo(ChatMessageListResultDto resultDto) {
		return ChatMessageListResponseVo.builder()
				.messages(resultDto.getMessages().stream().map(this::toMessageVo).toList())
				.build();
	}

	private ChatMessageListResponseVo.Message toMessageVo(ChatMessageItemDto itemDto) {
		return ChatMessageListResponseVo.Message.builder()
				.messageId(itemDto.getMessageId())
				.senderUuid(itemDto.getSenderUuid())
				.messageType(itemDto.getMessageType())
				.content(itemDto.getContent())
				.metadata(toMetadataVo(itemDto.getMetadata()))
				.createdAt(itemDto.getCreatedAt())
				.build();
	}

	private ChatMessageListResponseVo.Metadata toMetadataVo(ChatMessageMetadataDto metadataDto) {
		if (metadataDto == null) {
			return null;
		}
		return ChatMessageListResponseVo.Metadata.builder()
				.fileSize(metadataDto.getFileSize())
				.imageWidth(metadataDto.getImageWidth())
				.imageHeight(metadataDto.getImageHeight())
				.reservationId(metadataDto.getReservationId())
				.meetAt(metadataDto.getMeetAt())
				.price(metadataDto.getPrice())
				.placeName(metadataDto.getPlaceName())
				.latitude(metadataDto.getLatitude())
				.longitude(metadataDto.getLongitude())
				.build();
	}
}
