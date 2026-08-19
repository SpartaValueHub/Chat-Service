package com.sparta.chat_service.application.port.in;

import com.sparta.chat_service.application.port.in.dto.ChatRoomListResultDto;

// 내 1:1 채팅방 목록 조회
public interface ListChatRoomsUseCase {

	ChatRoomListResultDto list(String memberUuid);
}
