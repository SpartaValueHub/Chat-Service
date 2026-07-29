package com.sparta.chat_service.application.port.in;

import com.sparta.chat_service.application.port.dto.ChatMessageRequestDto;
import com.sparta.chat_service.application.port.dto.ChatRoomResponseDto;
import com.sparta.chat_service.application.port.dto.ChatRoomSaveDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatServiceUseCase {

    Mono<Void> sendChatMessage(ChatMessageRequestDto chatMessageRequestDto);

    Flux<ChatRoomResponseDto> getChatRooms();

    Mono<ChatRoomResponseDto> getChatRoomByUuid(String chatRoomUuid);

    Mono<ChatRoomResponseDto> createChatRoom(ChatRoomSaveDto chatRoomSaveDto);
}
