package com.sparta.chat_service.application.port.in;

import com.sparta.chat_service.application.port.in.dto.CreateChatRoomCommandDto;
import com.sparta.chat_service.application.port.in.dto.CreateChatRoomResultDto;

// 1:1 채팅방 생성. 같은 게시글·같은 쌍이면 기존 방 반환
public interface CreateChatRoomUseCase {

	CreateChatRoomResultDto create(CreateChatRoomCommandDto command);
}
