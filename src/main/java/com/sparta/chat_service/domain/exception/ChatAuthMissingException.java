package com.sparta.chat_service.domain.exception;

// Gateway가 주입한 회원 식별자가 없음
public class ChatAuthMissingException extends RuntimeException {

	// 안정적 에러 코드
	private final String code;

	public ChatAuthMissingException() {
		super("인증 정보가 없습니다.");
		this.code = "CHAT_AUTH_MISSING";
	}

	public String getCode() {
		return code;
	}
}
