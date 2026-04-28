package com.example.kafka.events;

import java.time.Instant;

public record UserRegisteredEvent(
        String eventId,
        String userId,
        String email,
        String name,
        Instant occurredAt,
        int eventVersion
) {
}
