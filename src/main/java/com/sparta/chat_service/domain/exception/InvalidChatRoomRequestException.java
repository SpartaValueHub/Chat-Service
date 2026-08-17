package com.sparta.chat_service.domain.exception;

// 방 생성 요청 값이 비어 있음
public class InvalidChatRoomRequestException extends RuntimeException {

	// 안정적 에러 코드
	private final String code;

	public InvalidChatRoomRequestException(String message) {
		super(message);
		this.code = "INVALID_REQUEST";
	}

	public String getCode() {
		return code;
	}
}
