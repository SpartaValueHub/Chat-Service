package com.sparta.chat_service.adaptor.in;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SeoulDateTimesTest {

	@Test
	void convertsUtcInstantToSeoulOffset() {
		Instant utc = Instant.parse("2026-08-20T05:00:00Z");

		OffsetDateTime seoul = SeoulDateTimes.toSeoul(utc);

		assertEquals(OffsetDateTime.parse("2026-08-20T14:00:00+09:00"), seoul);
		assertEquals(ZoneOffset.ofHours(9), seoul.getOffset());
	}

	@Test
	void returnsNullWhenInstantIsNull() {
		assertNull(SeoulDateTimes.toSeoul(null));
	}
}
