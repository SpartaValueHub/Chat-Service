package com.unionclass.chatting.application.port.in;

import reactor.core.publisher.Flux;

public interface ChatServiceReactiveUseCase {

    Flux<ChatMessageResponseDto> getChatByChatRoomUuid(String chatRoomUuid);
    Flux<ChatMessageResponseDto> getLatestChatByChatRoomUuid(String chatRoomUuid);

}
