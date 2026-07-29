package com.sparta.chat_service.adaptor.in.web.mapper;

import com.sparta.chat_service.adaptor.in.web.vo.ChatMessageRequestVo;
import com.sparta.chat_service.application.port.dto.ChatMessageRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper {

    public ChatMessageRequestDto toDto(ChatMessageRequestVo chatMessageRequestVo) {
        return ChatMessageRequestDto.builder()
                .chatRoomUuid(chatMessageRequestVo.getChatRoomUuid())
                .message(chatMessageRequestVo.getMessage())
                .messageType(chatMessageRequestVo.getMessageType())
                .senderUuid(chatMessageRequestVo.getSenderUuid())
                .build();
    }

}
