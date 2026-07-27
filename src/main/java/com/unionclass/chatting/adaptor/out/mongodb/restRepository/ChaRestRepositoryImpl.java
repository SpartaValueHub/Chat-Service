package com.unionclass.chatting.adaptor.out.mongodb.restRepository;

import com.unionclass.chatting.adaptor.out.mongodb.mapper.ChatEntityMapper;
import com.unionclass.chatting.application.port.dto.ChatMessageSaveDto;
import com.unionclass.chatting.application.port.out.ChatServiceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Repository
public class ChaRestRepositoryImpl implements ChatServiceRepositoryPort {

    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final ChatEntityMapper chatEntityMapper;

    @Override
    public Mono<Void> sendChatMessage(ChatMessageSaveDto chatMessageSaveDto) {
        return reactiveMongoTemplate.save(chatEntityMapper.toEntity(chatMessageSaveDto)).then();
    }
}
