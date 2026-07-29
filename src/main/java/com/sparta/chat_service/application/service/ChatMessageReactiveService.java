package com.sparta.chat_service.application.service;

import com.sparta.chat_service.application.mapper.ChatServiceMapper;
import com.sparta.chat_service.application.port.dto.ChatMessageResponseDto;
import com.sparta.chat_service.application.port.in.ChatServiceReactiveUseCase;
import com.sparta.chat_service.application.port.out.ChatServiceReactiveRepositoryPort;
import com.sparta.chat_service.domain.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Service
public class ChatMessageReactiveService implements ChatServiceReactiveUseCase {

    private final ChatServiceReactiveRepositoryPort chatServiceReactiveRepositoryPort;
    private final ChatServiceMapper chatServiceMapper;

    @Override
    public Flux<ChatMessageResponseDto> getChatByChatRoomUuid(String chatRoomUuid) {
        Flux<ChatMessage> getChat = chatServiceMapper.toChatMessage(
                chatServiceReactiveRepositoryPort.getChatByChatRoomUuid(chatRoomUuid));
        return chatServiceMapper.toChatMessageResponseDto(getChat);
    }

    @Override
    public Flux<ChatMessageResponseDto> getLatestChatByChatRoomUuid(String chatRoomUuid) {
        Flux<ChatMessage> getLatestChat = chatServiceMapper.toChatMessage(
                chatServiceReactiveRepositoryPort.getLatestChatByChatRoomUuid(chatRoomUuid)
        );
        return chatServiceMapper.toChatMessageResponseDto(getLatestChat);
    }


}
