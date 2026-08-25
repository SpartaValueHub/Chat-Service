package com.sparta.chat_service.domain.exception;

// S3 Presigned URL 발급 실패
public class ChatImageUploadUnavailableException extends RuntimeException {

	private final String code;

	public ChatImageUploadUnavailableException(String message) {
		super(message);
		this.code = "CHAT_IMAGE_UPLOAD_UNAVAILABLE";
	}

	public ChatImageUploadUnavailableException(String message, Throwable cause) {
		super(message, cause);
		this.code = "CHAT_IMAGE_UPLOAD_UNAVAILABLE";
	}

	public String getCode() {
		return code;
	}
}
