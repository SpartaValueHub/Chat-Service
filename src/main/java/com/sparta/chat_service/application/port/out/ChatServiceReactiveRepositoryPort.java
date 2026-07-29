package com.sparta.chat_service.application.port.out;

import com.sparta.chat_service.application.port.dto.ChatMessageGetDto;
import reactor.core.publisher.Flux;

public interface ChatServiceReactiveRepositoryPort {

    Flux<ChatMessageGetDto> getChatByChatRoomUuid(String chatRoomUuid);
    Flux<ChatMessageGetDto> getLatestChatByChatRoomUuid(String chatRoomUuid);

}
