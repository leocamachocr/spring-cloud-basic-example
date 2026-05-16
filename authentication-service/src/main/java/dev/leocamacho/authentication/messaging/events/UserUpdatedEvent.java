package dev.leocamacho.authentication.messaging.events;

import java.time.Instant;
import java.util.UUID;

public record UserUpdatedEvent(UUID userId, String email, String name, Instant occurredOn) {

    public static UserUpdatedEvent of(UUID userId, String email, String name) {
        return new UserUpdatedEvent(userId, email, name, Instant.now());
    }
}
