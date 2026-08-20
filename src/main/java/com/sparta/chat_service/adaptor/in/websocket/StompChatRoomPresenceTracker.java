package com.sparta.chat_service.adaptor.in.websocket;

import com.sparta.chat_service.application.port.out.ChatRoomPresencePort;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// 방 topic 구독 여부를 세션 단위로 추적
@Component
public class StompChatRoomPresenceTracker implements ChatRoomPresencePort {

	private final ConcurrentHashMap<String, SessionSubscriptions> sessions = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, Set<String>> viewingSessions = new ConcurrentHashMap<>();

	public void subscribe(String sessionId, Principal principal, String destination, String subscriptionId) {
		if (isBlank(sessionId) || principal == null || isBlank(principal.getName()) || isBlank(subscriptionId)) {
			return;
		}
		ChatRoomTopic.roomIdFrom(destination).ifPresent(roomId -> {
			String memberUuid = principal.getName().trim();
			SessionSubscriptions subscriptions = sessions.computeIfAbsent(sessionId, unused -> new SessionSubscriptions(memberUuid));
			String previousRoomId = subscriptions.roomIdsBySubscription.put(subscriptionId, roomId);
			if (previousRoomId != null && !previousRoomId.equals(roomId)) {
				removeViewing(memberUuid, previousRoomId, sessionId);
			}
			addViewing(memberUuid, roomId, sessionId);
		});
	}

	public void unsubscribe(String sessionId, String destination, String subscriptionId) {
		if (isBlank(sessionId)) {
			return;
		}
		SessionSubscriptions subscriptions = sessions.get(sessionId);
		if (subscriptions == null) {
			return;
		}
		String roomId = null;
		if (!isBlank(subscriptionId)) {
			roomId = subscriptions.roomIdsBySubscription.remove(subscriptionId);
		}
		if (roomId == null) {
			roomId = ChatRoomTopic.roomIdFrom(destination).orElse(null);
			if (roomId != null) {
				String matchedRoomId = roomId;
				subscriptions.roomIdsBySubscription.values().removeIf(matchedRoomId::equals);
			}
		}
		if (roomId != null) {
			removeViewing(subscriptions.memberUuid, roomId, sessionId);
		}
		if (subscriptions.roomIdsBySubscription.isEmpty()) {
			sessions.remove(sessionId, subscriptions);
		}
	}

	public void disconnect(String sessionId) {
		if (isBlank(sessionId)) {
			return;
		}
		SessionSubscriptions subscriptions = sessions.remove(sessionId);
		if (subscriptions == null) {
			return;
		}
		for (String roomId : Set.copyOf(subscriptions.roomIdsBySubscription.values())) {
			removeViewing(subscriptions.memberUuid, roomId, sessionId);
		}
	}

	@Override
	public boolean isViewing(String memberUuid, String roomId) {
		if (isBlank(memberUuid) || isBlank(roomId)) {
			return false;
		}
		Set<String> sessionIds = viewingSessions.get(viewingKey(memberUuid.trim(), roomId));
		return sessionIds != null && !sessionIds.isEmpty();
	}

	@EventListener
	public void onDisconnect(SessionDisconnectEvent event) {
		disconnect(event.getSessionId());
	}

	private void addViewing(String memberUuid, String roomId, String sessionId) {
		viewingSessions.computeIfAbsent(viewingKey(memberUuid, roomId), unused -> ConcurrentHashMap.newKeySet())
				.add(sessionId);
	}

	private void removeViewing(String memberUuid, String roomId, String sessionId) {
		String key = viewingKey(memberUuid, roomId);
		Set<String> sessionIds = viewingSessions.get(key);
		if (sessionIds == null) {
			return;
		}
		sessionIds.remove(sessionId);
		if (sessionIds.isEmpty()) {
			viewingSessions.remove(key, sessionIds);
		}
	}

	private String viewingKey(String memberUuid, String roomId) {
		return memberUuid + "|" + roomId;
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private static final class SessionSubscriptions {
		private final String memberUuid;
		private final ConcurrentHashMap<String, String> roomIdsBySubscription = new ConcurrentHashMap<>();

		private SessionSubscriptions(String memberUuid) {
			this.memberUuid = memberUuid;
		}
	}
}
