package dev.leocamacho.basic.messaging.events;

import java.time.Instant;
import java.util.UUID;

public record UserCreatedEvent(UUID userId, String email, String name, Instant occurredOn) {
}
