package com.sparta.chat_service.adaptor.in.web.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 채팅 이미지 Presigned PUT URL 발급 요청
@Getter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class IssuePresignedUploadRequestVo {

	private String contentType;
	private Long fileSize;
}
