package dev.leocamacho.authentication.messaging.publishers;

import dev.leocamacho.authentication.messaging.events.UserCreatedEvent;

public interface UserCreatedEventPublisher {
    void publish(UserCreatedEvent event);
}
