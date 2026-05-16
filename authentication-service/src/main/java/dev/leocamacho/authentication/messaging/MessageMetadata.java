package dev.leocamacho.authentication.messaging;

import java.util.UUID;

public record MessageMetadata(UUID correlationId, UUID userId) {

    public static final String HEADER_CORRELATION_ID = "x-correlation-id";
    public static final String HEADER_USER_ID = "x-user-id";

    public static MessageMetadata withGeneratedCorrelationId() {
        return new MessageMetadata(UUID.randomUUID(), null);
    }
}
