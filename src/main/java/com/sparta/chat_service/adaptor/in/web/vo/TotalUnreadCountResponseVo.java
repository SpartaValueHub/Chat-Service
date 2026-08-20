package com.sparta.chat_service.adaptor.in.web.vo;

import lombok.Builder;
import lombok.Getter;

// 헤더 채팅 아이콘용 미읽음 총합
@Getter
@Builder
public class TotalUnreadCountResponseVo {

	private final int totalUnreadCount;
}
