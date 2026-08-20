package com.sparta.chat_service.application.port.out;

import com.sparta.chat_service.domain.model.ChatMessage;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

// 채팅 메시지 조회
public interface LoadChatMessagePort {

	Optional<ChatMessage> findById(String messageId);

	List<ChatMessage> findLatestByRoomId(String roomId, int limit);

	List<ChatMessage> findByRoomIdBefore(String roomId, ChatMessage cursor, int limit);

	int countUnread(String roomId, String viewerUuid, Instant lastReadAt);
}
