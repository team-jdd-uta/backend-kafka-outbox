package com.example.kafka.userservice.dto;

import java.time.LocalDateTime;

public record UserResponse(
        String userId,
        String email,
        String name,
        LocalDateTime createdAt
) {
}
