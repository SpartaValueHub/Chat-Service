package com.sparta.chat_service.adaptor.out.mysql.repository;

import com.sparta.chat_service.adaptor.out.mysql.entity.ChatProductPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatProductPostJpaRepository extends JpaRepository<ChatProductPostEntity, String> {
}
