package dev.leocamacho.basic.messaging.listener;

import dev.leocamacho.basic.messaging.MessageMetadataMDCLoader;
import dev.leocamacho.basic.messaging.events.UserCreatedEvent;
import dev.leocamacho.basic.messaging.events.UserUpdatedEvent;
import dev.leocamacho.basic.messaging.handler.UserEventHandler;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    private final UserEventHandler handler;
    private final MessageMetadataMDCLoader mdcLoader;

    public UserEventListener(UserEventHandler handler, MessageMetadataMDCLoader mdcLoader) {
        this.handler = handler;
        this.mdcLoader = mdcLoader;
    }

    @RabbitListener(queues = "user.created.queue")
    public void onUserCreated(UserCreatedEvent event, Message message) {
        mdcLoader.execute(message.getMessageProperties(), () -> handler.handle(event));
    }

    @RabbitListener(queues = "user.updated.queue")
    public void onUserUpdated(UserUpdatedEvent event, Message message) {
        mdcLoader.execute(message.getMessageProperties(), () -> handler.handle(event));
    }
}
