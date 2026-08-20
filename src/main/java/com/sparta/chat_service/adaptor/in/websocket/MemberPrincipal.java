package com.sparta.chat_service.adaptor.in.websocket;

import java.security.Principal;

// STOMP 세션에 붙는 회원 식별자
public class MemberPrincipal implements Principal {

	private final String memberUuid;

	public MemberPrincipal(String memberUuid) {
		this.memberUuid = memberUuid;
	}

	@Override
	public String getName() {
		return memberUuid;
	}
}
