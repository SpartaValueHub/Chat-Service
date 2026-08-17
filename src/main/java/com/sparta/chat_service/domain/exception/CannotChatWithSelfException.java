package com.sparta.chat_service.domain.exception;

// 본인과의 1:1 채팅은 만들 수 없음
public class CannotChatWithSelfException extends RuntimeException {

	// 안정적 에러 코드
	private final String code;

	public CannotChatWithSelfException() {
		super("본인과는 채팅할 수 없습니다.");
		this.code = "CANNOT_CHAT_WITH_SELF";
	}

	public String getCode() {
		return code;
	}
}
