package com.unionclass.chatting.application.port.out;

import reactor.core.publisher.Flux;

public interface ChatServiceReactiveRepositoryPort {

    Flux<ChatMessageGetDto> getChatByChatRoomUuid(String chatRoomUuid);
    Flux<ChatMessageGetDto> getLatestChatByChatRoomUuid(String chatRoomUuid);

}
