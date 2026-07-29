package com.sparta.chat_service.adaptor.in.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class HealthController {

	@GetMapping("/health/test")
	public Mono<Map<String, String>> test() {
		return Mono.just(Map.of(
				"service", "chat-service",
				"status", "UP"
		));
	}

}
