package com.sparta.chat_service.adaptor.in.web;

import com.sparta.chat_service.adaptor.in.SeoulDateTimes;
import com.sparta.chat_service.adaptor.in.web.vo.ErrorResponseVo;
import com.sparta.chat_service.domain.exception.CannotChatWithSelfException;
import com.sparta.chat_service.domain.exception.ChatAuthMissingException;
import com.sparta.chat_service.domain.exception.ChatImageUploadUnavailableException;
import com.sparta.chat_service.domain.exception.ChatRoomAccessDeniedException;
import com.sparta.chat_service.domain.exception.ChatRoomNotFoundException;
import com.sparta.chat_service.domain.exception.ChatUserProfileNotFoundException;
import com.sparta.chat_service.domain.exception.InvalidChatRoomRequestException;
import com.sparta.chat_service.domain.exception.InvalidMemberUuidException;
import com.sparta.chat_service.domain.exception.MemberProfileUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ChatAuthMissingException.class)
	public ResponseEntity<ErrorResponseVo> handleAuthMissing(
			ChatAuthMissingException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.UNAUTHORIZED, exception.getCode(), exception.getMessage(), request);
	}

	@ExceptionHandler({InvalidChatRoomRequestException.class, InvalidMemberUuidException.class})
	public ResponseEntity<ErrorResponseVo> handleInvalidRequest(
			RuntimeException exception,
			HttpServletRequest request
	) {
		String code = exception instanceof InvalidChatRoomRequestException invalid
				? invalid.getCode()
				: ((InvalidMemberUuidException) exception).getCode();
		return error(HttpStatus.BAD_REQUEST, code, exception.getMessage(), request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponseVo> handleUnreadable(
			HttpMessageNotReadableException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청 본문이 올바르지 않습니다.", request);
	}

	@ExceptionHandler(CannotChatWithSelfException.class)
	public ResponseEntity<ErrorResponseVo> handleCannotChatWithSelf(
			CannotChatWithSelfException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.BAD_REQUEST, exception.getCode(), exception.getMessage(), request);
	}

	@ExceptionHandler(ChatRoomNotFoundException.class)
	public ResponseEntity<ErrorResponseVo> handleRoomNotFound(
			ChatRoomNotFoundException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.NOT_FOUND, exception.getCode(), exception.getMessage(), request);
	}

	@ExceptionHandler(ChatRoomAccessDeniedException.class)
	public ResponseEntity<ErrorResponseVo> handleRoomAccessDenied(
			ChatRoomAccessDeniedException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.FORBIDDEN, exception.getCode(), exception.getMessage(), request);
	}

	@ExceptionHandler(ChatUserProfileNotFoundException.class)
	public ResponseEntity<ErrorResponseVo> handleProfileNotFound(
			ChatUserProfileNotFoundException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.NOT_FOUND, exception.getCode(), exception.getMessage(), request);
	}

	@ExceptionHandler(MemberProfileUnavailableException.class)
	public ResponseEntity<ErrorResponseVo> handleMemberUnavailable(
			MemberProfileUnavailableException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.SERVICE_UNAVAILABLE, exception.getCode(), exception.getMessage(), request);
	}

	@ExceptionHandler(ChatImageUploadUnavailableException.class)
	public ResponseEntity<ErrorResponseVo> handleImageUploadUnavailable(
			ChatImageUploadUnavailableException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.SERVICE_UNAVAILABLE, exception.getCode(), exception.getMessage(), request);
	}

	private ResponseEntity<ErrorResponseVo> error(
			HttpStatus status,
			String code,
			String message,
			HttpServletRequest request
	) {
		return ResponseEntity.status(status)
				.body(ErrorResponseVo.builder()
						.timestamp(SeoulDateTimes.toSeoul(Instant.now()))
						.status(status.value())
						.code(code)
						.message(message)
						.path(request.getRequestURI())
						.build());
	}
}
