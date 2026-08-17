package com.sparta.chat_service.adaptor.out.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

	// 판매 상태
	@Column(name = "sale_status", length = 50, nullable = false)
	private String saleStatus;

	// 스냅샷 수정 시각
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Builder
	private ChatProductPostEntity(
			String productPostUuid,
			String productPostImageUrl,
			String productPostName,
			Long price,
			String saleStatus,
			LocalDateTime updatedAt
	) {
		this.productPostUuid = productPostUuid;
		this.productPostImageUrl = productPostImageUrl;
		this.productPostName = productPostName;
		this.price = price;
		this.saleStatus = saleStatus;
		this.updatedAt = updatedAt;
	}

	public static ChatProductPostEntity create(
			String productPostUuid,
			String productPostImageUrl,
			String productPostName,
			Long price,
			String saleStatus
	) {
		return ChatProductPostEntity.builder()
				.productPostUuid(productPostUuid)
				.productPostImageUrl(productPostImageUrl)
				.productPostName(productPostName)
				.price(price)
				.saleStatus(saleStatus)
				.updatedAt(LocalDateTime.now())
				.build();
	}

	// 상품 게시글상세 스냅샷 갱신
	public void update(String productPostImageUrl, String productPostName, Long price, String saleStatus) {
		this.productPostImageUrl = productPostImageUrl;
		this.productPostName = productPostName;
		this.price = price;
		this.saleStatus = saleStatus;
		this.updatedAt = LocalDateTime.now();
	}
}
