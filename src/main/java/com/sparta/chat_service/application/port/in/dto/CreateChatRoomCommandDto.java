package com.sparta.chat_service.application.port.in.dto;

import com.sparta.chat_service.domain.model.MemberGrade;
import lombok.Builder;
import lombok.Getter;

// 채팅방 생성 입력. 헤더 구매자 UUID + 상품 게시글상세 스냅샷
@Getter
@Builder
public class CreateChatRoomCommandDto {

	// 구매자 회원 UUID (X-Member-Uuid)
	private final String buyerUuid;
	// 상품 게시글 UUID
	private final String productPostUuid;
	// 판매자 회원 UUID
	private final String sellerUuid;
	// 게시글 이미지 URL
	private final String productPostImageUrl;
	// 게시글명
	private final String productPostName;
	// 가격
	private final Long price;
	// 판매 상태
	private final String saleStatus;
	// 판매자 닉네임
	private final String sellerNickname;
	// 판매자 회원 등급
	private final MemberGrade sellerMemberGrade;
}
