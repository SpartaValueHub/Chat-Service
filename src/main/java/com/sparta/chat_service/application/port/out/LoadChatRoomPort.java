package com.sparta.chat_service.application.port.out;

import com.sparta.chat_service.domain.model.ChatRoom;

import java.util.Optional;

// 상품 게시글 + 참여자 쌍으로 채팅방 조회
public interface LoadChatRoomPort {

	Optional<ChatRoom> findByProductPostAndMembers(
			String productPostUuid,
			String memberUuid1,
			String memberUuid2
	);
}
