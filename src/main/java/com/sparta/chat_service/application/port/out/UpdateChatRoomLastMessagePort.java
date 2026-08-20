package com.sparta.chat_service.application.port.out;

import com.sparta.chat_service.domain.model.LastMessage;

// 채팅방 마지막 메시지 미리보기만 갱신
public interface UpdateChatRoomLastMessagePort {

	void updateLastMessage(String roomId, LastMessage lastMessage);
}
