package com.sparta.chat_service.domain.model;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

// 채팅 이미지 S3 키. 접두사 chat/{yyyy}/{MM}/{dd}/{uuid}.{ext}
public final class ChatImageKey {

	public static final String PREFIX = "chat/";
	private static final Pattern S3_KEY_PATTERN = Pattern.compile(
			"^chat/[a-zA-Z0-9][a-zA-Z0-9/_-]*\\.(jpg|jpeg|png|webp|gif)$"
	);

	private ChatImageKey() {
	}

	public static String generate(ChatImageContentType contentType) {
		LocalDate date = LocalDate.now(ZoneOffset.UTC);
		return PREFIX
				+ date.getYear() + "/"
				+ pad2(date.getMonthValue()) + "/"
				+ pad2(date.getDayOfMonth()) + "/"
				+ UUID.randomUUID() + "."
				+ contentType.getExtension();
	}

	public static boolean isHttpUrl(String value) {
		if (value == null) {
			return false;
		}
		String normalized = value.trim().toLowerCase(Locale.ROOT);
		return normalized.startsWith("https://") || normalized.startsWith("http://");
	}

	public static boolean isValidS3Key(String value) {
		if (value == null) {
			return false;
		}
		String normalized = value.trim();
		return !normalized.contains("..") && S3_KEY_PATTERN.matcher(normalized).matches();
	}

	private static String pad2(int value) {
		return value < 10 ? "0" + value : String.valueOf(value);
	}
}
