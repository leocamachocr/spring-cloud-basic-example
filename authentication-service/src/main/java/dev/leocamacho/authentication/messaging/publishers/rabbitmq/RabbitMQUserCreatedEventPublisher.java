package dev.leocamacho.authentication.messaging.publishers.rabbitmq;

import dev.leocamacho.authentication.messaging.MessageMetadataPropagator;
import dev.leocamacho.authentication.messaging.events.UserCreatedEvent;
import dev.leocamacho.authentication.messaging.publishers.UserCreatedEventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQUserCreatedEventPublisher implements UserCreatedEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final MessageMetadataPropagator propagator;

    public RabbitMQUserCreatedEventPublisher(
            RabbitTemplate rabbitTemplate,
            MessageMetadataPropagator propagator
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.propagator = propagator;
    }

    @Override
    public void publish(UserCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                "user-events",
                "user.created",
                event, propagator.buildPostProcessor()
        );
    }
}
