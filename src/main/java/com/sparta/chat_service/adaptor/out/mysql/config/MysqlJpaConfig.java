package com.sparta.chat_service.adaptor.out.mysql.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// MySQL(JPA) 스캔 범위 한정
@Configuration
@EnableJpaRepositories(basePackages = "com.sparta.chat_service.adaptor.out.mysql")
@EntityScan(basePackages = "com.sparta.chat_service.adaptor.out.mysql.entity")
public class MysqlJpaConfig {
}
