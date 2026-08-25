package com.sparta.chat_service.adaptor.in.web.controller;

import com.sparta.chat_service.adaptor.in.web.vo.IssuePresignedUploadRequestVo;
import com.sparta.chat_service.adaptor.in.web.vo.IssuePresignedUploadResponseVo;
import com.sparta.chat_service.application.port.in.IssueChatImageUploadUrlUseCase;
import com.sparta.chat_service.application.port.in.dto.IssueChatImageUploadUrlCommandDto;
import com.sparta.chat_service.application.port.in.dto.IssueChatImageUploadUrlResultDto;
import com.sparta.chat_service.domain.exception.InvalidChatRoomRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@RestController
public class ChatMediaController {

	private static final String MEMBER_UUID_HEADER = "X-Member-Uuid";
	private static final String CONTENT_TYPE_HEADER = "Content-Type";

	private final IssueChatImageUploadUrlUseCase issueChatImageUploadUrlUseCase;

	@PostMapping("/rooms/{roomId}/images/presigned-url")
	public ResponseEntity<IssuePresignedUploadResponseVo> issuePresignedUrl(
			@RequestHeader(value = MEMBER_UUID_HEADER, required = false) String memberUuid,
			@PathVariable String roomId,
			@RequestBody IssuePresignedUploadRequestVo requestVo
	) {
		if (requestVo == null) {
			throw new InvalidChatRoomRequestException("요청 본문이 필요합니다.");
		}
		IssueChatImageUploadUrlResultDto resultDto = issueChatImageUploadUrlUseCase.issue(
				IssueChatImageUploadUrlCommandDto.builder()
						.memberUuid(memberUuid)
						.roomId(roomId)
						.contentType(requestVo.getContentType())
						.fileSize(requestVo.getFileSize())
						.build()
		);
		return ResponseEntity.ok(toVo(resultDto));
	}

	private IssuePresignedUploadResponseVo toVo(IssueChatImageUploadUrlResultDto resultDto) {
		return IssuePresignedUploadResponseVo.builder()
				.uploadUrl(resultDto.getUploadUrl())
				.method(resultDto.getMethod())
				.headers(Map.of(CONTENT_TYPE_HEADER, resultDto.getContentType()))
				.s3Key(resultDto.getS3Key())
				.publicUrl(resultDto.getPublicUrl())
				.expiresInSeconds(resultDto.getExpiresInSeconds())
				.build();
	}
}
