package com.sparta.chat_service.application.port.out;

import com.sparta.chat_service.domain.model.ChatRoom;

// 채팅방 저장
public interface SaveChatRoomPort {

	ChatRoom save(ChatRoom chatRoom);
}
