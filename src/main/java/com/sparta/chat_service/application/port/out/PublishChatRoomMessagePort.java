package com.sparta.chat_service.application.port.out;

import com.sparta.chat_service.application.port.in.dto.ChatMessageItemDto;

// 방 topic으로 저장된 말풍선 브로드캐스트
public interface PublishChatRoomMessagePort {

	void publish(String roomId, ChatMessageItemDto message);
}
