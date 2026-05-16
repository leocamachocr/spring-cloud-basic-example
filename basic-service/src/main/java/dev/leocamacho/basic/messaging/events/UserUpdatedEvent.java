package dev.leocamacho.basic.messaging.events;

import java.time.Instant;
import java.util.UUID;

public record UserUpdatedEvent(UUID userId, String email, String name, Instant occurredOn) {
}
