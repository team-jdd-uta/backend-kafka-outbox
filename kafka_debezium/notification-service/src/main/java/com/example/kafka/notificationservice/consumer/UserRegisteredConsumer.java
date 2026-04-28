package com.example.kafka.notificationservice.consumer;

import com.example.kafka.events.UserRegisteredEvent;
import com.example.kafka.notificationservice.repository.NotificationUserRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserRegisteredConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredConsumer.class);

    private final ObjectMapper objectMapper;
    private final NotificationUserRepository repository;

    public UserRegisteredConsumer(ObjectMapper objectMapper, NotificationUserRepository repository) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @KafkaListener(topics = "${app.kafka.topics.user-registered}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String payload) {
        UserRegisteredEvent event = deserialize(payload);
        log.info("Received user registered event: eventId={}, userId={}, email={}, name={}, occurredAt={}",
                event.eventId(), event.userId(), event.email(), event.name(), event.occurredAt());

        // Persist to the target DB (external_users) — this simulates moving user data to another database.
        repository.save(event.userId(), event.email(), event.name(), LocalDateTime.now());
    }

    private UserRegisteredEvent deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, UserRegisteredEvent.class);
        } catch (JacksonException exception) {
            throw new IllegalStateException("failed to deserialize user registered event", exception);
        }
    }
}
