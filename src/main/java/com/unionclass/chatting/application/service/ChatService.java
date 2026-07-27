package com.unionclass.chatting.application.service;

import com.unionclass.chatting.application.port.in.ChatServiceUseCase;
import com.unionclass.chatting.application.port.out.ChatServiceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService implements ChatServiceUseCase {

    private final ChatServiceRepositoryPort chatServiceRepositoryPort;


}
