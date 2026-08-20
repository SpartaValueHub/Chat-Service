package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.GetTotalUnreadCountUseCase;
import com.sparta.chat_service.application.port.in.dto.TotalUnreadCountResultDto;
import com.sparta.chat_service.application.port.out.LoadChatMessagePort;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.domain.exception.ChatAuthMissingException;
import com.sparta.chat_service.domain.model.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetTotalUnreadCountService implements GetTotalUnreadCountUseCase {

	private final LoadChatRoomPort loadChatRoomPort;
	private final LoadChatMessagePort loadChatMessagePort;

	@Override
	public TotalUnreadCountResultDto get(String memberUuid) {
		String viewerUuid = requireMemberUuid(memberUuid);
		int totalUnreadCount = loadChatRoomPort.findByParticipant(viewerUuid).stream()
				.mapToInt(room -> loadChatMessagePort.countUnread(
						room.getId(),
						viewerUuid,
						room.lastReadAt(viewerUuid).orElse(null)
				))
				.sum();
		return TotalUnreadCountResultDto.builder()
				.totalUnreadCount(totalUnreadCount)
				.build();
	}

	private String requireMemberUuid(String memberUuid) {
		String normalized = memberUuid == null ? "" : memberUuid.trim();
		if (normalized.isBlank()) {
			throw new ChatAuthMissingException();
		}
		return normalized;
	}
}
