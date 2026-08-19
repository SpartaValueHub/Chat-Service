package com.sparta.chat_service.adaptor.in.web.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 내 채팅방 목록 응답
@Getter
@Builder
public class ChatRoomListResponseVo {

	private final List<ChatRoomListItemResponseVo> rooms;
}
