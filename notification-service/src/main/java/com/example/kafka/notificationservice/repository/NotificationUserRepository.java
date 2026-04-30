package com.example.kafka.notificationservice.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class NotificationUserRepository {

    private final JdbcTemplate shard0JdbcTemplate;
    private final JdbcTemplate shard1JdbcTemplate;
    private final int shardCount;

    public NotificationUserRepository(
            @Qualifier("shard0JdbcTemplate") JdbcTemplate shard0JdbcTemplate,
            @Qualifier("shard1JdbcTemplate") JdbcTemplate shard1JdbcTemplate,
            @Value("${app.sharding.shard-count:2}") int shardCount
    ) {
        this.shard0JdbcTemplate = shard0JdbcTemplate;
        this.shard1JdbcTemplate = shard1JdbcTemplate;
        this.shardCount = shardCount;
    }

    public void save(String userId, String email, String name, LocalDateTime createdAt) {
        resolveJdbcTemplate(userId).update(
                "INSERT INTO customer (user_id, email, name, created_at) VALUES (?, ?, ?, ?)",
                userId, email, name, createdAt
        );
    }

    private JdbcTemplate resolveJdbcTemplate(String userId) {
        int shard = Math.floorMod(userId.hashCode(), shardCount);
        if (shard == 0) {
            return shard0JdbcTemplate;
        }
        return shard1JdbcTemplate;
    }
}
