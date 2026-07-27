package com.unionclass.chatting.application.port.out;

import com.unionclass.chatting.application.port.dto.ChatMessageSaveDto;

public interface ChatServiceRepositoryPort {

    void sendChatMessage(ChatMessageSaveDto chatMessageSaveDto);

}
