package com.sparta.chat_service.application.port.out;

import com.sparta.chat_service.domain.model.ChatRoom;

import java.util.List;
import java.util.Optional;

// 채팅방 조회
public interface LoadChatRoomPort {

	Optional<ChatRoom> findByProductPostAndMembers(
			String productPostUuid,
			String memberUuid1,
			String memberUuid2
	);

	Optional<ChatRoom> findById(String roomId);

	List<ChatRoom> findByParticipant(String memberUuid);
}
