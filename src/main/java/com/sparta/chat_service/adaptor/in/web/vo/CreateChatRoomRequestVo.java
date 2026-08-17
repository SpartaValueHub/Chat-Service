package com.sparta.chat_service.adaptor.in.web.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.sparta.chat_service.domain.model.MemberGrade;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 채팅방 생성 요청. 상품 게시글상세 스냅샷을 함께 받는다
@Getter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CreateChatRoomRequestVo {

	// 상품 게시글 UUID (프론트 상세에서 전달)
	private String productPostUuid;
	// 판매자 회원 UUID (프론트 상세에서 전달)
	private String sellerUuid;
	// 게시글 이미지 URL
	private String productPostImageUrl;
	// 게시글명
	private String productPostName;
	// 가격
	private Long price;
	// 판매 상태
	private String saleStatus;
	// 판매자 닉네임
	private String sellerNickname;
	// 판매자 회원 등급
	private MemberGrade sellerMemberGrade;
}
