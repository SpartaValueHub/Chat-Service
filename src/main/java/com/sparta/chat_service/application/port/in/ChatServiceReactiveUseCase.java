package com.sparta.chat_service.application.port.in;

import com.sparta.chat_service.application.port.dto.ChatMessageResponseDto;
import reactor.core.publisher.Flux;

public interface ChatServiceReactiveUseCase {

    Flux<ChatMessageResponseDto> getChatByChatRoomUuid(String chatRoomUuid);
    Flux<ChatMessageResponseDto> getLatestChatByChatRoomUuid(String chatRoomUuid);

}
