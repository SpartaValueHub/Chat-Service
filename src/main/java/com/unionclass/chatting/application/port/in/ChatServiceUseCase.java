package com.unionclass.chatting.application.port.in;

import com.unionclass.chatting.application.port.dto.ChatMessageRequestDto;

public interface ChatServiceUseCase {

    void sendChatMessage(ChatMessageRequestDto chatMessageRequestDto);

}
