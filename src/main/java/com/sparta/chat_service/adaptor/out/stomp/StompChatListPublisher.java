package com.sparta.chat_service.adaptor.out.stomp;

import com.sparta.chat_service.adaptor.in.websocket.vo.ChatListPreviewVo;
import com.sparta.chat_service.application.port.out.PublishChatListPreviewPort;
import com.sparta.chat_service.application.port.out.dto.ChatListPreviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

// 참여자 개인 큐로 목록 lastMessage 패치
@Component
@RequiredArgsConstructor
public class StompChatListPublisher implements PublishChatListPreviewPort {

	static final String CHAT_LIST_QUEUE = "/queue/chat-list";

	private final SimpMessagingTemplate messagingTemplate;

	@Override
	public void publish(List<String> memberUuids, ChatListPreviewDto preview) {
		if (preview == null || memberUuids == null || memberUuids.isEmpty()) {
			return;
		}
		ChatListPreviewVo payload = toVo(preview);
		for (String memberUuid : memberUuids) {
			if (memberUuid == null || memberUuid.isBlank()) {
				continue;
			}
			messagingTemplate.convertAndSendToUser(memberUuid.trim(), CHAT_LIST_QUEUE, payload);
		}
	}

	private ChatListPreviewVo toVo(ChatListPreviewDto preview) {
		ChatListPreviewDto.LastMessage lastMessage = preview.getLastMessage();
		return ChatListPreviewVo.builder()
				.roomId(preview.getRoomId())
				.lastMessage(lastMessage == null ? null : ChatListPreviewVo.LastMessage.builder()
						.content(lastMessage.getContent())
						.createdAt(lastMessage.getCreatedAt())
						.build())
				.unreadCount(preview.getUnreadCount())
				.updatedAt(preview.getUpdatedAt())
				.build();
	}
}
