package com.sparta.chat_service.adaptor.in.web.mapper;

import com.sparta.chat_service.adaptor.in.web.vo.ChatRoomRequestVo;
import com.sparta.chat_service.adaptor.in.web.vo.ChatRoomResponseVo;
import com.sparta.chat_service.application.port.dto.ChatRoomResponseDto;
import com.sparta.chat_service.application.port.dto.ChatRoomSaveDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class ChatRoomMapper {

    public Flux<ChatRoomResponseVo> toFluxVo(Flux<ChatRoomResponseDto> chatRoomResponseDtoFlux) {
        return chatRoomResponseDtoFlux.map(this::toVo);
    }

    public ChatRoomResponseVo toVo(ChatRoomResponseDto dto) {
        return ChatRoomResponseVo.builder()
                .chatRoomUuid(dto.getChatRoomUuid())
                .roomName(dto.getRoomName())
                .lastMessage(dto.getLastMessage())
                .lastMessageAt(dto.getLastMessageAt())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public ChatRoomSaveDto toSaveDto(ChatRoomRequestVo requestVo) {
        return ChatRoomSaveDto.builder()
                .chatRoomUuid(requestVo.getChatRoomUuid())
                .roomName(requestVo.getRoomName())
                .build();
    }
}
