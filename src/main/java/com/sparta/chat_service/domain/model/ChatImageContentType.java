package com.sparta.chat_service.domain.model;

import java.util.Locale;
import java.util.Optional;

// 채팅 이미지 허용 MIME
public enum ChatImageContentType {

	JPEG("image/jpeg", "jpg"),
	PNG("image/png", "png"),
	WEBP("image/webp", "webp"),
	GIF("image/gif", "gif");

	private final String mime;
	private final String extension;

	ChatImageContentType(String mime, String extension) {
		this.mime = mime;
		this.extension = extension;
	}

	public String getMime() {
		return mime;
	}

	public String getExtension() {
		return extension;
	}

	public static Optional<ChatImageContentType> fromMime(String contentType) {
		if (contentType == null) {
			return Optional.empty();
		}
		String normalized = contentType.trim().toLowerCase(Locale.ROOT);
		for (ChatImageContentType type : values()) {
			if (type.mime.equals(normalized)) {
				return Optional.of(type);
			}
		}
		return Optional.empty();
	}
}
