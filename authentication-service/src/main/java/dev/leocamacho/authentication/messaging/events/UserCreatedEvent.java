package dev.leocamacho.authentication.messaging.events;

import java.time.Instant;
import java.util.UUID;

public record UserCreatedEvent(UUID userId, String email, String name, Instant occurredOn) {

    public static UserCreatedEvent of(UUID userId, String email, String name) {
        return new UserCreatedEvent(userId, email, name, Instant.now());
    }
}
