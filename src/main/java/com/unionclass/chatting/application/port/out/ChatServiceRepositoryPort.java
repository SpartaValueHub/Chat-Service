package com.unionclass.chatting.application.port.out;

import com.unionclass.chatting.application.port.dto.ChatMessageSaveDto;
import reactor.core.publisher.Mono;

public interface ChatServiceRepositoryPort {

    Mono<Void> sendChatMessage(ChatMessageSaveDto chatMessageSaveDto);

}
