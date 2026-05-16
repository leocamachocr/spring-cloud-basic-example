package dev.leocamacho.basic.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "user-events";
    public static final String QUEUE_USER_CREATED = "user.created.queue";
    public static final String QUEUE_USER_UPDATED = "user.updated.queue";
    public static final String KEY_USER_CREATED = "user.created";
    public static final String KEY_USER_UPDATED = "user.updated";

    @Bean
    TopicExchange userEventsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    Queue userCreatedQueue() {
        return new Queue(QUEUE_USER_CREATED, true);
    }

    @Bean
    Queue userUpdatedQueue() {
        return new Queue(QUEUE_USER_UPDATED, true);
    }

    @Bean
    Binding userCreatedBinding(Queue userCreatedQueue, TopicExchange userEventsExchange) {
        return BindingBuilder.bind(userCreatedQueue).to(userEventsExchange).with(KEY_USER_CREATED);
    }

    @Bean
    Binding userUpdatedBinding(Queue userUpdatedQueue, TopicExchange userEventsExchange) {
        return BindingBuilder.bind(userUpdatedQueue).to(userEventsExchange).with(KEY_USER_UPDATED);
    }

    @Bean
    Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
