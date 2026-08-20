package com.sparta.chat_service.domain.exception;

// 채팅방이 없음
public class ChatRoomNotFoundException extends RuntimeException {

	// 안정적 에러 코드
	private final String code;

	public ChatRoomNotFoundException() {
		super("채팅방을 찾을 수 없습니다.");
		this.code = "CHAT_ROOM_NOT_FOUND";
	}

	public String getCode() {
		return code;
	}
}
