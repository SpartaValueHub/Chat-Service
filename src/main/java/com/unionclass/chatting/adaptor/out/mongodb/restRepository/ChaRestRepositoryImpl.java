package com.unionclass.chatting.adaptor.out.mongodb.restRepository;

import com.unionclass.chatting.adaptor.out.mongodb.mapper.ChatEntityMapper;
import com.unionclass.chatting.application.port.dto.ChatMessageSaveDto;
import com.unionclass.chatting.application.port.out.ChatServiceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ChaRestRepositoryImpl implements ChatServiceRepositoryPort {

    private final MongoTemplate mongoTemplate;
    private final ChatEntityMapper chatEntityMapper;

    @Override
    public void sendChatMessage(ChatMessageSaveDto chatMessageSaveDto) {
        mongoTemplate.save(chatEntityMapper.toEntity(chatMessageSaveDto));
    }
}
