package com.sparta.chat_service.adaptor.in.websocket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StompChatRoomPresenceTrackerTest {

	@Test
	void tracksSubscribeUnsubscribeAndDisconnect() {
		StompChatRoomPresenceTracker tracker = new StompChatRoomPresenceTracker();
		MemberPrincipal user = new MemberPrincipal("22222222-2222-4222-8222-222222222222");

		tracker.subscribe("s1", user, "/topic/chat.room-1", "sub-1");
		assertTrue(tracker.isViewing(user.getName(), "room-1"));

		tracker.unsubscribe("s1", "/topic/chat.room-1", "sub-1");
		assertFalse(tracker.isViewing(user.getName(), "room-1"));

		tracker.subscribe("s2", user, "/topic/chat.room-1", "sub-2");
		tracker.disconnect("s2");
		assertFalse(tracker.isViewing(user.getName(), "room-1"));
	}

	@Test
	void ignoresListQueueSubscriptions() {
		StompChatRoomPresenceTracker tracker = new StompChatRoomPresenceTracker();
		MemberPrincipal user = new MemberPrincipal("22222222-2222-4222-8222-222222222222");

		tracker.subscribe("s1", user, "/user/queue/chat-list", "sub-1");
		assertFalse(tracker.isViewing(user.getName(), "room-1"));
	}
}
