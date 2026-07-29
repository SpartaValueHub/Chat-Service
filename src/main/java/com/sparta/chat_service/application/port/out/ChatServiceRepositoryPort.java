package com.sparta.chat_service.application.port.out;

import com.sparta.chat_service.application.port.dto.ChatMessageSaveDto;
import com.sparta.chat_service.application.port.dto.ChatRoomGetDto;
import com.sparta.chat_service.application.port.dto.ChatRoomSaveDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatServiceRepositoryPort {

    Mono<Void> sendChatMessage(ChatMessageSaveDto chatMessageSaveDto);

    Flux<ChatRoomGetDto> getChatRooms();

    Mono<ChatRoomGetDto> getChatRoomByUuid(String chatRoomUuid);

    Mono<ChatRoomGetDto> saveChatRoom(ChatRoomSaveDto chatRoomSaveDto);
}
