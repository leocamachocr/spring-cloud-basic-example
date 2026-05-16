package dev.leocamacho.basic.messaging.handler;

import dev.leocamacho.basic.messaging.events.UserCreatedEvent;
import dev.leocamacho.basic.messaging.events.UserUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserEventHandler {

    private static final Logger log = LoggerFactory.getLogger(UserEventHandler.class);

    public void handle(UserCreatedEvent event) {
        log.info("User created: id={} email={}", event.userId(), event.email());
    }

    public void handle(UserUpdatedEvent event) {
        log.info("User updated: id={} email={}", event.userId(), event.email());
    }
}
