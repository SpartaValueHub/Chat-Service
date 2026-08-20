package com.sparta.chat_service.domain.exception;

// 채팅방 참여자가 아님
public class ChatRoomAccessDeniedException extends RuntimeException {

	// 안정적 에러 코드
	private final String code;

	public ChatRoomAccessDeniedException() {
		super("채팅방에 참여하지 않은 회원입니다.");
		this.code = "CHAT_ROOM_ACCESS_DENIED";
	}

	public String getCode() {
		return code;
	}
}
