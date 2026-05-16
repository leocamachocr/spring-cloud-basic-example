package dev.leocamacho.authentication.messaging.publishers.rabbitmq;

import dev.leocamacho.authentication.messaging.MessageMetadataPropagator;
import dev.leocamacho.authentication.messaging.events.UserUpdatedEvent;
import dev.leocamacho.authentication.messaging.publishers.UserUpdatedEventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQUserUpdatedEventPublisher implements UserUpdatedEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final MessageMetadataPropagator propagator;

    public RabbitMQUserUpdatedEventPublisher(RabbitTemplate rabbitTemplate,
                                             MessageMetadataPropagator propagator) {
        this.rabbitTemplate = rabbitTemplate;
        this.propagator = propagator;
    }

    @Override
    public void publish(UserUpdatedEvent event) {
        rabbitTemplate.convertAndSend("user-events", "user.updated", event, propagator.buildPostProcessor());
    }
}
