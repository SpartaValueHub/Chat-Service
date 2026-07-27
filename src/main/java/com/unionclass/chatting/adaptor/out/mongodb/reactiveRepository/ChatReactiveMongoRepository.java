package com.unionclass.chatting.adaptor.out.mongodb.reactiveRepository;

import com.unionclass.chatting.adaptor.out.mongodb.entity.ChatMessageEntity;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;

public interface ChatReactiveMongoRepository extends ReactiveMongoRepository<ChatMessageEntity, String> {

    @Tailable
    @Query("{ 'chatRoomUuid' : ?0 }")
    Flux<ChatMessageEntity> findByChatRoomUuid(String chatRoomUuid);

}
