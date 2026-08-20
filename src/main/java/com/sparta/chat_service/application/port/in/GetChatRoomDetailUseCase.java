package com.sparta.chat_service.application.port.in;

import com.sparta.chat_service.application.port.in.dto.ChatRoomDetailResultDto;

// 채팅방 상세 조회 (상품 상단 + 판매자 닉네임)
public interface GetChatRoomDetailUseCase {

	ChatRoomDetailResultDto get(String memberUuid, String roomId);
}
