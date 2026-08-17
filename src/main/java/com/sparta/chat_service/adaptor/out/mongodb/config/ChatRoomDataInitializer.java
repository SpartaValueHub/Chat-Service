package com.sparta.chat_service.adaptor.out.mongodb.config;

import com.sparta.chat_service.adaptor.out.mongodb.entity.ChatRoomEntity;
import com.sparta.chat_service.adaptor.out.mongodb.entity.ParticipantDocument;
import com.sparta.chat_service.domain.model.ChatRoomStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomDataInitializer implements ApplicationRunner {

	private final ReactiveMongoTemplate reactiveMongoTemplate;

	private static final List<SeedRoom> DEFAULT_ROOMS = List.of(
			new SeedRoom(
					"aaaaaaaaaaaaaaaaaaaaaaaa",
					"11111111-1111-4111-8111-111111111111",
					List.of("22222222-2222-4222-8222-222222222222", "33333333-3333-4333-8333-333333333333")
			),
			new SeedRoom(
					"bbbbbbbbbbbbbbbbbbbbbbbb",
					"44444444-4444-4444-8444-444444444444",
					List.of("22222222-2222-4222-8222-222222222222", "55555555-5555-4555-8555-555555555555")
			)
	);

	@Override
	public void run(ApplicationArguments args) {
		Flux.fromIterable(DEFAULT_ROOMS)
				.flatMap(this::upsertIfAbsent)
				.then()
				.doOnSuccess(unused -> log.info("Chat room seed completed"))
				.doOnError(error -> log.warn("Chat room seed failed: {}", error.getMessage()))
				.subscribe();
	}

	private Mono<ChatRoomEntity> upsertIfAbsent(SeedRoom seedRoom) {
		Query query = Query.query(Criteria.where("_id").is(seedRoom.id()));
		Instant now = Instant.now();

		return reactiveMongoTemplate.exists(query, ChatRoomEntity.class)
				.flatMap(exists -> {
					if (Boolean.TRUE.equals(exists)) {
						return Mono.empty();
					}
					List<ParticipantDocument> participants = seedRoom.memberUuids().stream()
							.map(memberUuid -> ParticipantDocument.builder()
									.memberUuid(memberUuid)
									.inRoom(true)
									.joinedAt(now)
									.build())
							.toList();

					return reactiveMongoTemplate.save(ChatRoomEntity.builder()
							.id(seedRoom.id())
							.productPostUuid(seedRoom.productPostUuid())
							.participants(participants)
							.lastMessage(null)
							.status(ChatRoomStatus.ACTIVE.name())
							.createdAt(now)
							.updatedAt(now)
							.build());
				});
	}

	private record SeedRoom(String id, String productPostUuid, List<String> memberUuids) {
	}
}
