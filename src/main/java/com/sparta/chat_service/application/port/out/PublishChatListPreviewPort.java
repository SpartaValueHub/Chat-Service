package com.sparta.chat_service.application.port.out;

import com.sparta.chat_service.application.port.out.dto.ChatListPreviewDto;

import java.util.List;

// 채팅 목록 미리보기를 참여자 소켓으로 푸시
public interface PublishChatListPreviewPort {

	void publish(List<String> memberUuids, ChatListPreviewDto preview);
}
