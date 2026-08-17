package com.sparta.chat_service.adaptor.out.mysql.repository;

import com.sparta.chat_service.adaptor.out.mysql.entity.ChatUserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatUserProfileJpaRepository extends JpaRepository<ChatUserProfileEntity, String> {
}
