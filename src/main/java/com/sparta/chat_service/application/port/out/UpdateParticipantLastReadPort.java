package com.sparta.chat_service.application.port.out;

import java.time.Instant;

// 참여자 lastRead만 갱신. 방 updated_at은 건드리지 않음
public interface UpdateParticipantLastReadPort {

	void updateLastRead(String roomId, String memberUuid, Instant lastReadAt);
}
