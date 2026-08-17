package com.sparta.chat_service.domain.exception;

// memberUuid가 비어 있거나 형식이 올바르지 않음
public class InvalidMemberUuidException extends RuntimeException {

	// 안정적 에러 코드
	private final String code;

	public InvalidMemberUuidException() {
		super("memberUuid는 필수입니다.");
		this.code = "INVALID_REQUEST";
	}

	public String getCode() {
		return code;
	}
}
