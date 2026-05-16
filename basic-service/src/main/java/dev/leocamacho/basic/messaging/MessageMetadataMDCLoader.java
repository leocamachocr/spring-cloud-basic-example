package dev.leocamacho.basic.messaging;

import dev.leocamacho.basic.session.Session;
import dev.leocamacho.basic.session.SessionContextHolder;
import org.slf4j.MDC;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MessageMetadataMDCLoader {

    public static final String HEADER_CORRELATION_ID = "x-correlation-id";
    public static final String HEADER_USER_ID = "x-user-id";

    public void execute(MessageProperties props, Runnable task) {
        UUID correlationId = extractCorrelationId(props);
        String userId = extractUserId(props);

        Session session = Session.newBuilder()
                .withCorrelationId(correlationId)
                .buildAnonymous();

        SessionContextHolder.setSession(session);
        MDC.put("correlationId", correlationId.toString());
        if (userId != null) {
            MDC.put("userId", userId);
        }

        try {
            task.run();
        } finally {
            MDC.remove("correlationId");
            MDC.remove("userId");
            SessionContextHolder.clearSession();
        }
    }

    private UUID extractCorrelationId(MessageProperties props) {
        Object raw = props.getHeader(HEADER_CORRELATION_ID);
        if (raw != null) {
            try {
                return UUID.fromString(raw.toString());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return UUID.randomUUID();
    }

    private String extractUserId(MessageProperties props) {
        Object raw = props.getHeader(HEADER_USER_ID);
        return raw != null ? raw.toString() : null;
    }
}
