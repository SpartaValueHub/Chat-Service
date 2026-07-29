package com.sparta.chat_service.application.mapper;

import com.sparta.chat_service.application.port.dto.ChatMessageGetDto;
import com.sparta.chat_service.application.port.dto.ChatMessageRequestDto;
import com.sparta.chat_service.application.port.dto.ChatMessageResponseDto;
import com.sparta.chat_service.application.port.dto.ChatMessageSaveDto;
import com.sparta.chat_service.application.port.dto.ChatRoomGetDto;
import com.sparta.chat_service.application.port.dto.ChatRoomResponseDto;
import com.sparta.chat_service.domain.model.ChatMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class ChatServiceMapper {

    public Flux<ChatMessageResponseDto> toChatMessageResponseDto(Flux<ChatMessage> chatMessageFlux) {
        return chatMessageFlux.map(chatMessage ->
                ChatMessageResponseDto.builder()
                        .chatMessageUuid(chatMessage.getChatMessageUuid())
                        .chatRoomUuid(chatMessage.getChatRoomUuid())
                        .messageType(chatMessage.getMessageType())
                        .message(chatMessage.getMessage())
                        .senderUuid(chatMessage.getSenderUuid())
                        .createdAt(chatMessage.getCreatedAt())
                        .updatedAt(chatMessage.getUpdatedAt())
                        .build()
        );
    }

    public Flux<ChatMessage> toChatMessage(Flux<ChatMessageGetDto> chatMessageGetDtoFlux) {
        return chatMessageGetDtoFlux.map(chatMessageGetDto ->
                ChatMessage.builder()
                        .chatMessageUuid(chatMessageGetDto.getChatMessageUuid())
                        .chatRoomUuid(chatMessageGetDto.getChatRoomUuid())
                        .messageType(chatMessageGetDto.getMessageType())
                        .message(chatMessageGetDto.getMessage())
                        .senderUuid(chatMessageGetDto.getSenderUuid())
                        .createdAt(chatMessageGetDto.getCreatedAt())
                        .updatedAt(chatMessageGetDto.getUpdatedAt())
                        .build()
        );
    }

    public ChatMessageSaveDto toChatMessageSaveDto(ChatMessage chatMessage) {
        return ChatMessageSaveDto.builder()
                .chatRoomUuid(chatMessage.getChatRoomUuid())
                .messageType(chatMessage.getMessageType())
                .message(chatMessage.getMessage())
                .senderUuid(chatMessage.getSenderUuid())
                .build();
    }

    public ChatMessage fromChatMessageRequestDto(ChatMessageRequestDto chatMessageRequestDto) {
        return ChatMessage.builder()
                .chatRoomUuid(chatMessageRequestDto.getChatRoomUuid())
                .messageType(chatMessageRequestDto.getMessageType())
                .message(chatMessageRequestDto.getMessage())
                .senderUuid(chatMessageRequestDto.getSenderUuid())
                .build();
    }

    public Flux<ChatRoomResponseDto> toChatRoomResponseDtoFlux(Flux<ChatRoomGetDto> chatRoomGetDtoFlux) {
        return chatRoomGetDtoFlux.map(this::toChatRoomResponseDto);
    }

    public ChatRoomResponseDto toChatRoomResponseDto(ChatRoomGetDto chatRoomGetDto) {
        return ChatRoomResponseDto.builder()
                .chatRoomUuid(chatRoomGetDto.getChatRoomUuid())
                .roomName(chatRoomGetDto.getRoomName())
                .lastMessage(chatRoomGetDto.getLastMessage())
                .lastMessageAt(chatRoomGetDto.getLastMessageAt())
                .createdAt(chatRoomGetDto.getCreatedAt())
                .updatedAt(chatRoomGetDto.getUpdatedAt())
                .build();
    }

}
