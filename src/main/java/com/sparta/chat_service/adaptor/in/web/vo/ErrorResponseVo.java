package com.sparta.chat_service.adaptor.in.web.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

// 공통 에러 응답
@Getter
@Builder
public class ErrorResponseVo {

	// ISO-8601 시각 (Asia/Seoul)
	private final OffsetDateTime timestamp;
	// HTTP 상태 코드
	private final int status;
	// 안정적 에러 코드
	private final String code;
	// 사용자 메시지
	private final String message;
	// 요청 경로
	private final String path;
	// 필드 검증 오류
	private final List<FieldErrorVo> fieldErrors;

	@Getter
	@Builder
	public static class FieldErrorVo {

		// 필드명
		private final String field;
		// 필드 오류 코드
		private final String code;
		// 필드 오류 메시지
		private final String message;
	}
}
