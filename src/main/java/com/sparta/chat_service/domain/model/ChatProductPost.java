package com.sparta.chat_service.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 채팅용 상품 게시글 스냅샷 (MySQL)
@Getter
public class ChatProductPost {

	// 상품 게시글 UUID
	private final String productPostUuid;
	// 게시글 이미지 URL
	private final String productPostImageUrl;
	// 게시글명
	private final String productPostName;
	// 가격
	private final Long price;
	// 거래 상태
	private final TradeStatus tradeStatus;
	// 스냅샷 수정 시각
	private final LocalDateTime updatedAt;

	@Builder
	private ChatProductPost(
			String productPostUuid,
			String productPostImageUrl,
			String productPostName,
			Long price,
			TradeStatus tradeStatus,
			LocalDateTime updatedAt
	) {
		this.productPostUuid = productPostUuid;
		this.productPostImageUrl = productPostImageUrl;
		this.productPostName = productPostName;
		this.price = price;
		this.tradeStatus = tradeStatus;
		this.updatedAt = updatedAt;
	}

	public static ChatProductPost create(
			String productPostUuid,
			String productPostImageUrl,
			String productPostName,
			Long price,
			TradeStatus tradeStatus
	) {
		return ChatProductPost.builder()
				.productPostUuid(productPostUuid)
				.productPostImageUrl(productPostImageUrl)
				.productPostName(productPostName)
				.price(price)
				.tradeStatus(tradeStatus)
				.updatedAt(LocalDateTime.now())
				.build();
	}

	public static ChatProductPost restore(
			String productPostUuid,
			String productPostImageUrl,
			String productPostName,
			Long price,
			TradeStatus tradeStatus,
			LocalDateTime updatedAt
	) {
		return ChatProductPost.builder()
				.productPostUuid(productPostUuid)
				.productPostImageUrl(productPostImageUrl)
				.productPostName(productPostName)
				.price(price)
				.tradeStatus(tradeStatus)
				.updatedAt(updatedAt)
				.build();
	}

	// 상품 게시글상세 스냅샷 갱신
	public ChatProductPost update(
			String productPostImageUrl,
			String productPostName,
			Long price,
			TradeStatus tradeStatus
	) {
		return ChatProductPost.builder()
				.productPostUuid(this.productPostUuid)
				.productPostImageUrl(productPostImageUrl)
				.productPostName(productPostName)
				.price(price)
				.tradeStatus(tradeStatus)
				.updatedAt(LocalDateTime.now())
				.build();
	}
}
