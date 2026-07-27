package com.unionclass.chatting.adaptor.out.mongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class ChatRoomEntity {

    @Id
    private String id;
    private String roomName;
    private List<ParticipantEntity> participantEntityList;

}
