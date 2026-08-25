package com.sparta.chat_service.application.service;

import com.sparta.chat_service.domain.model.ChatImageKey;
import com.sparta.chat_service.domain.model.MessageType;

// IMAGE 메시지의 s3Key를 CloudFront URL로 조립. 기존 http URL은 그대로 둔다
public class ChatImageUrlResolver {

	private final String cloudfrontBaseUrl;

	public ChatImageUrlResolver(String cloudfrontBaseUrl) {
		this.cloudfrontBaseUrl = cloudfrontBaseUrl;
	}

	public String toResponseContent(MessageType messageType, String storedContent) {
		if (messageType != MessageType.IMAGE) {
			return storedContent;
		}
		return toPublicUrl(storedContent);
	}

	public String toPublicUrl(String storedContent) {
		if (storedContent == null) {
			return null;
		}
		String value = storedContent.trim();
		if (value.isEmpty() || ChatImageKey.isHttpUrl(value)) {
			return value.isEmpty() ? storedContent : value;
		}
		String base = cloudfrontBaseUrl == null ? "" : cloudfrontBaseUrl.trim();
		if (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		String key = value.startsWith("/") ? value.substring(1) : value;
		if (base.isEmpty()) {
			return key;
		}
		return base + "/" + key;
	}
}
