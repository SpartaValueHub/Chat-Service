package com.sparta.chat_service.application.port.out;

import com.sparta.chat_service.domain.model.ChatProductPost;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

// 채팅용 상품 게시글 스냅샷 조회
public interface LoadChatProductPostPort {

	List<ChatProductPost> findAllByProductPostUuids(Collection<String> productPostUuids);

	Optional<ChatProductPost> findByProductPostUuid(String productPostUuid);
}
