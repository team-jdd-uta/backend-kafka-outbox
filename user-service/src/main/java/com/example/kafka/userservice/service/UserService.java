package com.example.kafka.userservice.service;

import com.example.kafka.events.UserRegisteredEvent;
import com.example.kafka.userservice.domain.OutboxEvent;
import com.example.kafka.userservice.domain.User;
import com.example.kafka.userservice.dto.CreateUserRequest;
import com.example.kafka.userservice.dto.UserResponse;
import com.example.kafka.userservice.mapper.OutboxEventMapper;
import com.example.kafka.userservice.mapper.UserMapper;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final String AGGREGATE_TYPE = "user";
    private static final String EVENT_TYPE = "UserRegistered";
    private static final String EVENT_STATUS_PENDING = "PENDING";

    private final UserMapper userMapper;
    private final OutboxEventMapper outboxEventMapper;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public UserService(
            UserMapper userMapper,
            OutboxEventMapper outboxEventMapper,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper
    ) {
        this.userMapper = userMapper;
        this.outboxEventMapper = outboxEventMapper;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UserResponse register(CreateUserRequest request) {
        if (userMapper.findByEmail(request.email()) != null) {
            throw new DuplicateEmailException(request.email());
        }

        LocalDateTime now = LocalDateTime.now();
        long nowMillis = Instant.now().toEpochMilli();
        String userId = UUID.randomUUID().toString();
        String eventId = UUID.randomUUID().toString();

        User user = new User();
        user.setUserId(userId);
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setCreatedAt(now);
        userMapper.insert(user);

        UserRegisteredEvent event = new UserRegisteredEvent(
                eventId,
                userId,
                request.email(),
                request.name(),
                Instant.now(),
                1
        );

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventId(eventId);
        outboxEvent.setAggregateType(AGGREGATE_TYPE);
        outboxEvent.setAggregateId(userId);
        outboxEvent.setEventType(EVENT_TYPE);
        outboxEvent.setPayload(serializeEvent(event));
        outboxEvent.setStatus(EVENT_STATUS_PENDING);
        outboxEvent.setCreatedAt(nowMillis);
        outboxEvent.setPublishedAt(null);
        outboxEventMapper.insert(outboxEvent);

        return new UserResponse(userId, request.email(), request.name(), now);
    }

    private String serializeEvent(UserRegisteredEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JacksonException exception) {
            throw new IllegalStateException("failed to serialize user registered event", exception);
        }
    }
}
