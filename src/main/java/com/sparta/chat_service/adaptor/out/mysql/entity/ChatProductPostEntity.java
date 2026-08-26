package com.sparta.chat_service.adaptor.out.mysql.entity;

import com.sparta.chat_service.domain.model.ProductPostStatus;
import com.sparta.chat_service.domain.model.TradeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// chat_product_posts 테이블 매핑
@Entity
@Table(name = "chat_product_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatProductPostEntity {

	// 상품 게시글 UUID (PK)
	@Id
	@Column(name = "product_post_uuid", length = 36, nullable = false)
	private String productPostUuid;

	// 게시글 이미지 URL
	@Column(name = "product_post_image_url", length = 500)
	private String productPostImageUrl;

	// 게시글명
	@Column(name = "product_post_name", length = 200, nullable = false)
	private String productPostName;

	// 가격
	@Column(name = "price", nullable = false)
	private Long price;

	// 거래 상태
	@Enumerated(EnumType.STRING)
	@Column(name = "trade_status", nullable = false, length = 20)
	private TradeStatus tradeStatus;

	// 상품 게시글 상태 (PUBLIC: 등록됨, DELETED: 삭제됨)
	@Enumerated(EnumType.STRING)
	@Column(name = "product_post_status", nullable = false, length = 20)
	private ProductPostStatus productPostStatus;

	// 스냅샷 수정 시각
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Builder
	private ChatProductPostEntity(
			String productPostUuid,
			String productPostImageUrl,
			String productPostName,
			Long price,
			TradeStatus tradeStatus,
			ProductPostStatus productPostStatus,
			LocalDateTime updatedAt
	) {
		this.productPostUuid = productPostUuid;
		this.productPostImageUrl = productPostImageUrl;
		this.productPostName = productPostName;
		this.price = price;
		this.tradeStatus = tradeStatus;
		this.productPostStatus = productPostStatus;
		this.updatedAt = updatedAt;
	}

	public static ChatProductPostEntity create(
			String productPostUuid,
			String productPostImageUrl,
			String productPostName,
			Long price,
			TradeStatus tradeStatus,
			ProductPostStatus productPostStatus
	) {
		return ChatProductPostEntity.builder()
				.productPostUuid(productPostUuid)
				.productPostImageUrl(productPostImageUrl)
				.productPostName(productPostName)
				.price(price)
				.tradeStatus(tradeStatus)
				.productPostStatus(productPostStatus)
				.updatedAt(LocalDateTime.now())
				.build();
	}

	// 상품 게시글상세 스냅샷 갱신
	public void update(
			String productPostImageUrl,
			String productPostName,
			Long price,
			TradeStatus tradeStatus,
			ProductPostStatus productPostStatus
	) {
		this.productPostImageUrl = productPostImageUrl;
		this.productPostName = productPostName;
		this.price = price;
		this.tradeStatus = tradeStatus;
		this.productPostStatus = productPostStatus;
		this.updatedAt = LocalDateTime.now();
	}
}
