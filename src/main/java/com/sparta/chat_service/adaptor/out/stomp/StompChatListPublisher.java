package com.sparta.chat_service.adaptor.out.stomp;

import com.sparta.chat_service.adaptor.in.SeoulDateTimes;
import com.sparta.chat_service.adaptor.in.websocket.vo.ChatListPreviewVo;
import com.sparta.chat_service.application.port.out.PublishChatListPreviewPort;
import com.sparta.chat_service.application.port.out.dto.ChatListPreviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

// 해당 회원 개인 큐로 목록 lastMessage·unreadCount·상품 스냅샷 패치
@Component
@RequiredArgsConstructor
public class StompChatListPublisher implements PublishChatListPreviewPort {

	static final String CHAT_LIST_QUEUE = "/queue/chat-list";

	private final SimpMessagingTemplate messagingTemplate;

	@Override
	public void publish(String memberUuid, ChatListPreviewDto preview) {
		if (preview == null || memberUuid == null || memberUuid.isBlank()) {
			return;
		}
		messagingTemplate.convertAndSendToUser(memberUuid.trim(), CHAT_LIST_QUEUE, toVo(preview));
	}

	private ChatListPreviewVo toVo(ChatListPreviewDto preview) {
		ChatListPreviewDto.LastMessage lastMessage = preview.getLastMessage();
		return ChatListPreviewVo.builder()
				.roomId(preview.getRoomId())
				.lastMessage(lastMessage == null ? null : ChatListPreviewVo.LastMessage.builder()
						.content(lastMessage.getContent())
						.createdAt(SeoulDateTimes.toSeoul(lastMessage.getCreatedAt()))
						.build())
				.unreadCount(preview.getUnreadCount())
				.updatedAt(SeoulDateTimes.toSeoul(preview.getUpdatedAt()))
				.productPost(toProductPost(preview.getProductPost()))
				.build();
	}

	private ChatListPreviewVo.ProductPost toProductPost(ChatListPreviewDto.ProductPost productPost) {
		if (productPost == null) {
			return null;
		}
		return ChatListPreviewVo.ProductPost.builder()
				.productPostUuid(productPost.getProductPostUuid())
				.productPostImageUrl(productPost.getProductPostImageUrl())
				.productPostName(productPost.getProductPostName())
				.price(productPost.getPrice())
				.tradeStatus(productPost.getTradeStatus())
				.build();
	}
}
