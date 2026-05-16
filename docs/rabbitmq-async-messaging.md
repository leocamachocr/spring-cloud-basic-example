# Asynchronous messaging with RabbitMQ

This guide explains how asynchronous communication between microservices was implemented using
RabbitMQ and Spring AMQP. It covers service configuration, infrastructure setup, the chosen
architecture, and the message types involved.

---

## 1. Service configuration

Each service that participates in messaging needs the AMQP starter and its connection properties.

### 1.1 Adding the dependency

Add `spring-boot-starter-amqp` to the `dependencies` block of each service's `build.gradle.kts`:

```kotlin
implementation("org.springframework.boot:spring-boot-starter-amqp")
```

This single starter brings in the RabbitMQ client library, `RabbitTemplate` (for sending),
and the `@RabbitListener` infrastructure (for receiving).

### 1.2 Connection properties — `authentication-service`

```yaml
# authentication-service/src/main/resources/application.yml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASS:guest}
```

### 1.3 Connection properties — `basic-service`

```yaml
# basic-service/src/main/resources/application.yml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASS:guest}
```

Both blocks are identical. Environment variables allow overriding defaults without touching the
YAML in deployed environments.

### 1.4 Broker topology — `RabbitMQConfig`

Spring AMQP declares exchanges, queues, and bindings as beans. The broker creates them the first
time the application connects if they do not already exist (idempotent).

Both services declare the same topology so that whichever service starts first takes ownership of
creating the infrastructure.

```java
// Relevant constants used across both services
public static final String EXCHANGE         = "user-events";
public static final String QUEUE_CREATED    = "user.created.queue";
public static final String QUEUE_UPDATED    = "user.updated.queue";
public static final String KEY_CREATED      = "user.created";
public static final String KEY_UPDATED      = "user.updated";
```

```java
@Bean TopicExchange userEventsExchange() {
    return new TopicExchange("user-events");
}

@Bean Queue userCreatedQueue() {
    return new Queue("user.created.queue", true); // durable
}

@Bean Binding userCreatedBinding(Queue userCreatedQueue, TopicExchange userEventsExchange) {
    return BindingBuilder.bind(userCreatedQueue)
                         .to(userEventsExchange)
                         .with("user.created");
}
```

A **topic exchange** was chosen so that routing keys follow the `domain.action` naming convention
and future subscribers can use wildcard patterns (`user.#`) without reconfiguring the producer.

### 1.5 JSON serialization

By default Spring AMQP serializes messages using Java serialization, which is fragile across
service versions. Both services configure a `Jackson2JsonMessageConverter` and wire it into the
`RabbitTemplate`:

```java
@Bean
Jackson2JsonMessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
}

// Producer side only — wires the converter into the template
@Bean
RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                               Jackson2JsonMessageConverter messageConverter) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(messageConverter);
    return template;
}
```

The consumer picks up the same `Jackson2JsonMessageConverter` bean automatically when it is
present in the context, so no additional wiring is needed on that side.

---

## 2. Docker Compose — RabbitMQ broker

```yaml
# docker-compose.yml (project root)
services:
  rabbitmq:
    image: rabbitmq:3-management
    container_name: rabbitmq
    ports:
      - "5672:5672"   # AMQP protocol — used by the services
      - "15672:15672" # Management UI — http://localhost:15672
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq  # persists queues and messages across restarts

volumes:
  rabbitmq_data:
```

**Start the broker:**

```bash
docker compose up -d
```

Open `http://localhost:15672` to access the management UI (credentials: `guest` / `guest`).
From there you can inspect exchanges, queues, bindings, and message throughput in real time.

---

## 3. Implementation structure

### 3.1 Overview

```
authentication-service  (producer)
└── messaging/
    ├── events/
    │   ├── UserCreatedEvent.java          ← domain event (record)
    │   └── UserUpdatedEvent.java          ← domain event (record)
    ├── publishers/
    │   ├── UserCreatedEventPublisher.java ← output port (interface)
    │   ├── UserUpdatedEventPublisher.java ← output port (interface)
    │   └── rabbitmq/
    │       ├── RabbitMQUserCreatedEventPublisher.java  ← RabbitMQ adapter
    │       └── RabbitMQUserUpdatedEventPublisher.java  ← RabbitMQ adapter
    ├── MessageMetadata.java               ← header constants + record
    └── MessageMetadataPropagator.java     ← reads session → builds MessagePostProcessor

basic-service  (consumer)
└── messaging/
    ├── events/
    │   ├── UserCreatedEvent.java          ← local copy of the event (record)
    │   └── UserUpdatedEvent.java          ← local copy of the event (record)
    ├── handler/
    │   └── UserEventHandler.java          ← business logic for received events
    ├── listener/
    │   └── UserEventListener.java         ← @RabbitListener entry point
    └── MessageMetadataMDCLoader.java      ← extracts headers → loads MDC + session
```

### 3.2 Producer — dependency inversion

The handler that creates a user (`RegisterUserHandler`) depends on the **interface**
`UserCreatedEventPublisher`, not on any RabbitMQ class:

```java
// handlers/commands/RegisterUserHandler.java
@Component
public class RegisterUserHandler {
    @Autowired private UserCreatedEventPublisher userCreatedEventPublisher;

    public void register(Command command) {
        // ... validation and persistence ...
        UserEntity saved = repository.save(user);
        userCreatedEventPublisher.publish(
            UserCreatedEvent.of(saved.getId(), saved.getEmail(), saved.getName())
        );
    }
}
```

The actual RabbitMQ implementation is injected by Spring at runtime:

```java
// messaging/publishers/rabbitmq/RabbitMQUserCreatedEventPublisher.java
@Component
public class RabbitMQUserCreatedEventPublisher implements UserCreatedEventPublisher {

    public void publish(UserCreatedEvent event) {
        rabbitTemplate.convertAndSend(
            "user-events", "user.created", event, propagator.buildPostProcessor()
        );
    }
}
```

This means the business handler can be unit-tested with a mock publisher without needing a
running RabbitMQ instance.

### 3.3 Consumer — listener + handler separation

`UserEventListener` is the AMQP entry point. It is intentionally thin: it extracts metadata from
the message and delegates all business logic to `UserEventHandler`.

```java
// messaging/listener/UserEventListener.java
@Component
public class UserEventListener {

    @RabbitListener(queues = "user.created.queue")
    public void onUserCreated(UserCreatedEvent event, Message message) {
        mdcLoader.execute(message.getMessageProperties(), () -> handler.handle(event));
    }

    @RabbitListener(queues = "user.updated.queue")
    public void onUserUpdated(UserUpdatedEvent event, Message message) {
        mdcLoader.execute(message.getMessageProperties(), () -> handler.handle(event));
    }
}
```

Spring AMQP deserializes the JSON body into the typed event automatically, and injects the raw
`Message` object as a second parameter so the listener can read the AMQP headers.

---

## 4. Metadata propagation (correlationId and userId)

One of the key cross-cutting concerns in a distributed system is **traceability**: being able to
link a log line produced by a consumer to the original HTTP request that triggered the message.

### 4.1 The problem

When an HTTP request arrives, `SessionFilter` places a `correlationId` in `MDC` so that every
log line written during that request includes the ID. But when a message is published to RabbitMQ,
the consumer runs in a different thread and a different process — MDC is thread-local, so the
`correlationId` is lost.

### 4.2 Solution — AMQP message headers

The `correlationId` (and optionally the `userId`) are attached as custom AMQP headers when the
message is sent, and restored into MDC when the message is received.

#### 4.2.1 Sending — `MessageMetadataPropagator`

```java
// messaging/MessageMetadataPropagator.java
@Component
public class MessageMetadataPropagator {

    public MessagePostProcessor buildPostProcessor() {
        MessageMetadata metadata = buildFromCurrentSession();
        return message -> {
            message.getMessageProperties()
                   .setHeader(MessageMetadata.HEADER_CORRELATION_ID,
                              metadata.correlationId().toString());
            if (metadata.userId() != null) {
                message.getMessageProperties()
                       .setHeader(MessageMetadata.HEADER_USER_ID,
                                  metadata.userId().toString());
            }
            return message;
        };
    }

    private MessageMetadata buildFromCurrentSession() {
        Session session = SessionContextHolder.getSession();
        if (session == null) {
            return MessageMetadata.withGeneratedCorrelationId(); // no HTTP context, generate one
        }
        UUID correlationId = session.correlationId() != null
                ? session.correlationId() : UUID.randomUUID();
        return new MessageMetadata(correlationId, session.id()); // userId may be null
    }
}
```

Rule: `correlationId` is **always** present in the message. If the sender has no active session
(e.g., a background job), a new UUID is generated. `userId` is included only when there is an
authenticated session.

#### 4.2.2 Receiving — `MessageMetadataMDCLoader`

```java
// messaging/MessageMetadataMDCLoader.java
@Component
public class MessageMetadataMDCLoader {

    public void execute(MessageProperties props, Runnable task) {
        UUID correlationId = extractCorrelationId(props); // generates if header is missing
        String userId      = extractUserId(props);        // null if header is absent

        Session session = Session.newBuilder()
                .withCorrelationId(correlationId)
                .buildAnonymous();

        SessionContextHolder.setSession(session);
        MDC.put("correlationId", correlationId.toString());
        if (userId != null) {
            MDC.put("userId", userId);
        }

        try {
            task.run(); // executes the business handler
        } finally {
            MDC.remove("correlationId");
            MDC.remove("userId");
            SessionContextHolder.clearSession();
        }
    }
}
```

This mirrors `SessionFilter.doFilter()` exactly: set up context → run business logic → clean up
in `finally` regardless of exceptions.

### 4.3 Header constants

Both services share the same header names via their local `MessageMetadata`/`MessageMetadataMDCLoader`
constants:

| Constant              | Header name         | Type   | Required |
|-----------------------|---------------------|--------|----------|
| `HEADER_CORRELATION_ID` | `x-correlation-id` | UUID   | Yes      |
| `HEADER_USER_ID`        | `x-user-id`        | UUID   | No       |

### 4.4 Log pattern

With `correlationId` and `userId` in MDC, the log pattern in `basic-service` is:

```
%5p [basic-service,<correlationId>,<userId>]
```

A log line from a message-driven handler will look like:

```
 INFO [basic-service,3f2a1c9d-...,] - User created: id=... email=...      (no userId)
 INFO [basic-service,3f2a1c9d-...,8e4b0f2a-...] - User created: id=...   (with userId)
```

---

## 5. Message types

### `UserCreatedEvent`

Published by `authentication-service` immediately after a new user is persisted.

```java
public record UserCreatedEvent(UUID userId, String email, String name, Instant occurredOn) {

    public static UserCreatedEvent of(UUID userId, String email, String name) {
        return new UserCreatedEvent(userId, email, name, Instant.now());
    }
}
```

| Field        | Type      | Description                              |
|--------------|-----------|------------------------------------------|
| `userId`     | UUID      | Stable identifier assigned at creation   |
| `email`      | String    | User's email address                     |
| `name`       | String    | User's display name                      |
| `occurredOn` | Instant   | UTC timestamp of when the event occurred |

- **Exchange:** `user-events`
- **Routing key:** `user.created`
- **Queue:** `user.created.queue`

### `UserUpdatedEvent`

Published whenever a user's profile is modified.

```java
public record UserUpdatedEvent(UUID userId, String email, String name, Instant occurredOn) {

    public static UserUpdatedEvent of(UUID userId, String email, String name) {
        return new UserUpdatedEvent(userId, email, name, Instant.now());
    }
}
```

Same fields as `UserCreatedEvent`. The distinction is communicated by the routing key.

- **Exchange:** `user-events`
- **Routing key:** `user.updated`
- **Queue:** `user.updated.queue`

### Why local copies of events?

Each service owns its own copy of the event records instead of sharing a library. This is a
deliberate trade-off:

- **No compile-time coupling** between producer and consumer — each team can evolve independently.
- **Explicit contract** — the consumer only maps the fields it actually needs.
- **Downside** — field names must be kept in sync manually; a JSON schema registry (e.g., Confluent
  Schema Registry) would enforce this automatically at scale.
