package com.sparta.chat_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

// 헤더 채팅 아이콘용 미읽음 총합
@Getter
@Builder
public class TotalUnreadCountResultDto {

	private final int totalUnreadCount;
}
