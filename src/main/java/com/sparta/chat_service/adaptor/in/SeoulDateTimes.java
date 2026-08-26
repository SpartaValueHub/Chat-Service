package com.sparta.chat_service.adaptor.in;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

// API 응답 시각은 Asia/Seoul(+09:00)로 내려준다. 저장은 Instant(UTC)를 유지한다.
public final class SeoulDateTimes {

	public static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

	private SeoulDateTimes() {
	}

	public static OffsetDateTime toSeoul(Instant instant) {
		if (instant == null) {
			return null;
		}
		return instant.atZone(SEOUL).toOffsetDateTime();
	}
}
