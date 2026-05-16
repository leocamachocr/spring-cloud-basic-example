package dev.leocamacho.authentication.messaging;

import dev.leocamacho.authentication.session.Session;
import dev.leocamacho.authentication.session.SessionContextHolder;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MessageMetadataPropagator {

    public MessagePostProcessor buildPostProcessor() {
        MessageMetadata metadata = buildFromCurrentSession();
        return message -> {
            message.getMessageProperties().setHeader(
                    MessageMetadata.HEADER_CORRELATION_ID, metadata.correlationId().toString());
            if (metadata.userId() != null) {
                message.getMessageProperties().setHeader(
                        MessageMetadata.HEADER_USER_ID, metadata.userId().toString());
            }
            return message;
        };
    }

    private MessageMetadata buildFromCurrentSession() {
        Session session = SessionContextHolder.getSession();
        if (session == null) {
            return MessageMetadata.withGeneratedCorrelationId();
        }
        UUID correlationId = session.correlationId() != null ? session.correlationId() : UUID.randomUUID();
        return new MessageMetadata(correlationId, session.id());
    }
}
