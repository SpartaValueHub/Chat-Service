package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.port.in.IssueChatImageUploadUrlUseCase;
import com.sparta.chat_service.application.port.in.dto.IssueChatImageUploadUrlCommandDto;
import com.sparta.chat_service.application.port.in.dto.IssueChatImageUploadUrlResultDto;
import com.sparta.chat_service.application.port.out.IssuePresignedUploadPort;
import com.sparta.chat_service.application.port.out.LoadChatRoomPort;
import com.sparta.chat_service.application.port.out.dto.PresignedUploadDto;
import com.sparta.chat_service.config.ChatMediaProperties;
import com.sparta.chat_service.domain.exception.ChatAuthMissingException;
import com.sparta.chat_service.domain.exception.ChatRoomAccessDeniedException;
import com.sparta.chat_service.domain.exception.ChatRoomNotFoundException;
import com.sparta.chat_service.domain.exception.InvalidChatRoomRequestException;
import com.sparta.chat_service.domain.model.ChatImageContentType;
import com.sparta.chat_service.domain.model.ChatImageKey;
import com.sparta.chat_service.domain.model.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssueChatImageUploadUrlService implements IssueChatImageUploadUrlUseCase {

	private static final String PUT_METHOD = "PUT";

	private final LoadChatRoomPort loadChatRoomPort;
	private final IssuePresignedUploadPort issuePresignedUploadPort;
	private final ChatImageUrlResolver chatImageUrlResolver;
	private final ChatMediaProperties chatMediaProperties;

	@Override
	public IssueChatImageUploadUrlResultDto issue(IssueChatImageUploadUrlCommandDto command) {
		if (command == null) {
			throw new InvalidChatRoomRequestException("요청 본문이 필요합니다.");
		}
		String memberUuid = requireMemberUuid(command.getMemberUuid());
		String roomId = requireText(command.getRoomId(), "roomId는 필수입니다.");
		requireAccessibleRoom(memberUuid, roomId);
		ChatImageContentType contentType = ChatImageContentType.fromMime(command.getContentType())
				.orElseThrow(() -> new InvalidChatRoomRequestException(
						"허용하지 않는 이미지 형식입니다. image/jpeg, image/png, image/webp, image/gif만 가능합니다."));
		requireFileSize(command.getFileSize());
		String s3Key = ChatImageKey.generate(contentType);
		PresignedUploadDto issued = issuePresignedUploadPort.issuePutUrl(
				s3Key,
				contentType.getMime(),
				chatMediaProperties.getPresignExpireSeconds()
		);
		return IssueChatImageUploadUrlResultDto.builder()
				.uploadUrl(issued.getUploadUrl())
				.method(PUT_METHOD)
				.contentType(contentType.getMime())
				.s3Key(issued.getS3Key())
				.publicUrl(chatImageUrlResolver.toPublicUrl(issued.getS3Key()))
				.expiresInSeconds(issued.getExpiresInSeconds())
				.build();
	}

	private void requireFileSize(Long fileSize) {
		if (fileSize == null || fileSize < 1) {
			throw new InvalidChatRoomRequestException("fileSize는 1 이상이어야 합니다.");
		}
		long maxBytes = chatMediaProperties.getMaxFileSizeBytes();
		if (fileSize > maxBytes) {
			throw new InvalidChatRoomRequestException("이미지 용량은 " + maxBytes + "바이트를 초과할 수 없습니다.");
		}
	}

	private ChatRoom requireAccessibleRoom(String memberUuid, String roomId) {
		ChatRoom room = loadChatRoomPort.findById(roomId)
				.orElseThrow(ChatRoomNotFoundException::new);
		if (!room.hasParticipant(memberUuid)) {
			throw new ChatRoomAccessDeniedException();
		}
		return room;
	}

	private String requireMemberUuid(String memberUuid) {
		String normalized = memberUuid == null ? "" : memberUuid.trim();
		if (normalized.isBlank()) {
			throw new ChatAuthMissingException();
		}
		return normalized;
	}

	private String requireText(String value, String message) {
		String normalized = value == null ? "" : value.trim();
		if (normalized.isBlank()) {
			throw new InvalidChatRoomRequestException(message);
		}
		return normalized;
	}
}
