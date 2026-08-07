package com.sparta.chat_service.adaptor.out.mongodb.reactiveRepository;

import com.sparta.chat_service.adaptor.out.mongodb.entity.ChatMessageEntity;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ChatReactiveMongoRepository extends ReactiveMongoRepository<ChatMessageEntity, String> {

	@Tailable
	@Query("{ 'room_id' : ?0 }")
	Flux<ChatMessageEntity> findByRoomId(String roomId);
}
