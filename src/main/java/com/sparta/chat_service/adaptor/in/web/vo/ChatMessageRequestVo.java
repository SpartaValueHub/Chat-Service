package com.sparta.chat_service.adaptor.in.web.vo;

import lombok.Getter;

@Getter
public class ChatMessageRequestVo {

    private String chatRoomUuid;
    private String messageType;
    private String message;
    private String senderUuid;

}
