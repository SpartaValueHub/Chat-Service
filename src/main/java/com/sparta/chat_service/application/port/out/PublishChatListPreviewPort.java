package com.sparta.chat_service.application.port.out;

import com.sparta.chat_service.application.port.out.dto.ChatListPreviewDto;

// 채팅 목록 미리보기를 해당 회원 소켓으로 푸시
public interface PublishChatListPreviewPort {

	void publish(String memberUuid, ChatListPreviewDto preview);
}
