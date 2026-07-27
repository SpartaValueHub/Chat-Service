package com.unionclass.chatting.adaptor.out.mongodb.mapper;

import com.unionclass.chatting.adaptor.out.mongodb.entity.ChatMessageEntity;
import com.unionclass.chatting.application.port.dto.ChatMessageGetDto;
import com.unionclass.chatting.application.port.dto.ChatMessageSaveDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Instant;

@Component
public class ChatEntityMapper {

    public Flux<ChatMessageGetDto> chatMessageGetDtoFlux(Flux<ChatMessageEntity> chatMessageEntityFlux) {
        return chatMessageEntityFlux.map(chatMessageEntity ->
                ChatMessageGetDto.builder()
                        .chatMessageUuid(chatMessageEntity.getId())
                        .chatRoomUuid(chatMessageEntity.getChatRoomUuid())
                        .message(chatMessageEntity.getMessage())
                        .messageType(chatMessageEntity.getMessageType())
                        .senderUuid(chatMessageEntity.getSenderUuid())
                        .createdAt(chatMessageEntity.getCreatedAt() != null ? chatMessageEntity.getCreatedAt().toString() : null)
                        .updatedAt(chatMessageEntity.getUpdatedAt() != null ? chatMessageEntity.getUpdatedAt().toString() : null)
                        .build()
                );
    }


    public ChatMessageEntity toEntity(ChatMessageSaveDto chatMessageSaveDto) {
        Instant now = Instant.now();
        return ChatMessageEntity.builder()
                .chatRoomUuid(chatMessageSaveDto.getChatRoomUuid())
                .messageType(chatMessageSaveDto.getMessageType())
                .message(chatMessageSaveDto.getMessage())
                .senderUuid(chatMessageSaveDto.getSenderUuid())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

}
