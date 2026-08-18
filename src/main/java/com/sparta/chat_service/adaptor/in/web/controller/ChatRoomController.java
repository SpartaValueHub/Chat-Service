package com.sparta.chat_service.adaptor.in.web.controller;

import com.sparta.chat_service.adaptor.in.web.vo.CreateChatRoomRequestVo;
import com.sparta.chat_service.adaptor.in.web.vo.CreateChatRoomResponseVo;
import com.sparta.chat_service.application.port.in.CreateChatRoomUseCase;
import com.sparta.chat_service.application.port.in.dto.CreateChatRoomCommandDto;
import com.sparta.chat_service.application.port.in.dto.CreateChatRoomResultDto;
import com.sparta.chat_service.domain.exception.InvalidChatRoomRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@RestController
public class ChatRoomController {

	// Gateway가 주입하는 구매자 UUID 헤더
	private static final String MEMBER_UUID_HEADER = "X-Member-Uuid";

	// 채팅방 생성 유스케이스
	private final CreateChatRoomUseCase createChatRoomUseCase;

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
				.saleStatus(requestVo.getSaleStatus())
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
}
