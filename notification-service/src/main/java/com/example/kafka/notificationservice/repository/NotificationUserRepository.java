package com.example.kafka.notificationservice.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class NotificationUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(String userId, String email, String name, LocalDateTime createdAt) {
        jdbcTemplate.update(
                "INSERT INTO customer (user_id, name, created_at) VALUES (?, ?, ?)",
                userId, name, createdAt
        );
    }
}
