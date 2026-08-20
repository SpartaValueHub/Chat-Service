package com.sparta.chat_service.application.port.out;

// 해당 방 topic을 지금 구독 중인지
public interface ChatRoomPresencePort {

	boolean isViewing(String memberUuid, String roomId);
}
