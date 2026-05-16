package dev.leocamacho.authentication.handlers.commands;

import dev.leocamacho.authentication.exceptions.BusinessException;
import dev.leocamacho.authentication.exceptions.InvalidInputException;
import dev.leocamacho.authentication.jpa.entities.UserEntity;
import dev.leocamacho.authentication.jpa.repositories.UserRepository;
import dev.leocamacho.authentication.messaging.events.UserCreatedEvent;
import dev.leocamacho.authentication.messaging.publishers.UserCreatedEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegisterUserHandler {
    @Autowired
    private UserRepository repository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private UserCreatedEventPublisher userCreatedEventPublisher;

    public record Command(String email, String name, String password) {
    }

    public void register(Command command) {
        validateRequiredFields(command);
        validateExistingUser(command.email());
        UserEntity user = new UserEntity();
        user.setEmail(command.email());
        user.setName(command.name());
        user.setPassword(encoder.encode(command.password()));
        user.setRoles(List.of("ACCOUNT_MANAGER"));
        UserEntity saved = repository.save(user);
        userCreatedEventPublisher.publish(UserCreatedEvent.of(saved.getId(), saved.getEmail(), saved.getName()));
    }

    private void validateExistingUser(String email) {
        if (repository.findByEmail(email).isPresent()) {
            throw new BusinessException("User already exists");
        }
    }

    private void validateRequiredFields(Command command) {
        if (command.email() == null) {
            throw new InvalidInputException("email");
        }
        if (command.name() == null) {
            throw new InvalidInputException("name");
        }
        if (command.password() == null) {
            throw new InvalidInputException("password");
        }


    }
}
