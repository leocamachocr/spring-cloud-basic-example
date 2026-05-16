package dev.leocamacho.authentication.messaging.publishers;

import dev.leocamacho.authentication.messaging.events.UserUpdatedEvent;

public interface UserUpdatedEventPublisher {
    void publish(UserUpdatedEvent event);
}
