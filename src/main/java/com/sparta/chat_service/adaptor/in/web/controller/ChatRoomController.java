package com.sparta.chat_service.adaptor.in.web.controller;

import com.sparta.chat_service.adaptor.in.web.vo.ChatRoomListItemResponseVo;
import com.sparta.chat_service.adaptor.in.web.vo.ChatRoomListResponseVo;
import com.sparta.chat_service.adaptor.in.web.vo.CreateChatRoomRequestVo;
import com.sparta.chat_service.adaptor.in.web.vo.CreateChatRoomResponseVo;
import com.sparta.chat_service.application.port.in.CreateChatRoomUseCase;
import com.sparta.chat_service.application.port.in.ListChatRoomsUseCase;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@RestController
public class ChatRoomController {

	// Gateway가 주입하는 회원 UUID 헤더
	private static final String MEMBER_UUID_HEADER = "X-Member-Uuid";

	private final CreateChatRoomUseCase createChatRoomUseCase;
	private final ListChatRoomsUseCase listChatRoomsUseCase;

	@GetMapping("/rooms")
	public ResponseEntity<ChatRoomListResponseVo> listChatRooms(
			@RequestHeader(value = MEMBER_UUID_HEADER, required = false) String memberUuid
	) {
		ChatRoomListResultDto resultDto = listChatRoomsUseCase.list(memberUuid);
		return ResponseEntity.ok(toListVo(resultDto));
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
}
