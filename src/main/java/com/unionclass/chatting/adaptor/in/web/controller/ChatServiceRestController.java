package com.unionclass.chatting.adaptor.in.web.controller;

import com.unionclass.chatting.adaptor.in.web.mapper.ChatMessageMapper;
import com.unionclass.chatting.adaptor.in.web.vo.ChatMessageRequestVo;
import com.unionclass.chatting.application.port.in.ChatServiceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@RestController
public class ChatServiceRestController {

    private final ChatServiceUseCase chatServiceUseCase;
    private final ChatMessageMapper chatMessageMapper;

    @PostMapping("/send")
    public void sendChatMessage(
            @RequestBody ChatMessageRequestVo chatMessageRequestVo
    ) {
        chatServiceUseCase.sendChatMessage( chatMessageMapper.toDto(
                chatMessageRequestVo
        ));
    }

}
