package com.unionclass.chatting.application.port.in;

import com.unionclass.chatting.application.port.dto.ChatMessageRequestDto;
import reactor.core.publisher.Mono;

public interface ChatServiceUseCase {

    Mono<Void> sendChatMessage(ChatMessageRequestDto chatMessageRequestDto);

}
