package com.sparta.chat_service.application.port.in;

import com.sparta.chat_service.application.port.in.dto.TotalUnreadCountResultDto;

// 내 채팅 미읽음 총합
public interface GetTotalUnreadCountUseCase {

	TotalUnreadCountResultDto get(String memberUuid);
}
