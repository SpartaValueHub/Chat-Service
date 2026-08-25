package com.sparta.chat_service.application.port.in;

import com.sparta.chat_service.application.port.in.dto.IssueChatImageUploadUrlCommandDto;
import com.sparta.chat_service.application.port.in.dto.IssueChatImageUploadUrlResultDto;

// 채팅 이미지 S3 Presigned PUT URL 발급
public interface IssueChatImageUploadUrlUseCase {

	IssueChatImageUploadUrlResultDto issue(IssueChatImageUploadUrlCommandDto command);
}
