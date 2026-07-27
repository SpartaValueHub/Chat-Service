package com.unionclass.chatting.application.service;

import com.unionclass.chatting.application.mapper.ChatServiceMapper;
import com.unionclass.chatting.application.port.dto.ChatMessageRequestDto;
import com.unionclass.chatting.application.port.in.ChatServiceUseCase;
import com.unionclass.chatting.application.port.out.ChatServiceRepositoryPort;
import com.unionclass.chatting.domain.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService implements ChatServiceUseCase {

    private final ChatServiceRepositoryPort chatServiceRepositoryPort;
    private final ChatServiceMapper chatServiceMapper;


    @Override
    public void sendChatMessage(ChatMessageRequestDto chatMessageRequestDto) {
        ChatMessage chatMessage = chatServiceMapper.fromChatMessageRequestDto(chatMessageRequestDto);
        chatServiceRepositoryPort.sendChatMessage(chatServiceMapper.toChatMessageSaveDto(chatMessage));
    }

}
