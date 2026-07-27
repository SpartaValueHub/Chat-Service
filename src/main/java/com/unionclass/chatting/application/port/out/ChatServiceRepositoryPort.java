package com.unionclass.chatting.application.port.out;

public interface ChatServiceRepositoryPort {

    void sendChatMessage(ChatMessageSaveDto chatMessageSaveDto);

}
