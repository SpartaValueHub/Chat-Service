package com.sparta.chat_service.domain.exception;

// 로컬 Read Model·Member 모두에서 프로필을 찾지 못함
public class ChatUserProfileNotFoundException extends RuntimeException {

	// 안정적 에러 코드
	private final String code;
	// 조회에 사용한 회원 UUID
	private final String memberUuid;

	public ChatUserProfileNotFoundException(String memberUuid) {
		super("채팅 회원 프로필을 찾을 수 없습니다.");
		this.code = "CHAT_USER_PROFILE_NOT_FOUND";
		this.memberUuid = memberUuid;
	}

	public String getCode() {
		return code;
	}

	public String getMemberUuid() {
		return memberUuid;
	}
}
