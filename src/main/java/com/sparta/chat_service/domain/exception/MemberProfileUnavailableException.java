package com.sparta.chat_service.domain.exception;

// Member 서비스 호출 실패 (네트워크·5xx 등)
public class MemberProfileUnavailableException extends RuntimeException {

	// 안정적 에러 코드
	private final String code;

	public MemberProfileUnavailableException(String message, Throwable cause) {
		super(message, cause);
		this.code = "MEMBER_PROFILE_UNAVAILABLE";
	}

	public MemberProfileUnavailableException(String message) {
		super(message);
		this.code = "MEMBER_PROFILE_UNAVAILABLE";
	}

	public String getCode() {
		return code;
	}
}
