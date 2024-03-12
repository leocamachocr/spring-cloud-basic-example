package dev.leocamacho.authentication.handlers.commands;

import dev.leocamacho.authentication.exceptions.BusinessException;
import dev.leocamacho.authentication.exceptions.InvalidInputException;
import dev.leocamacho.authentication.jpa.entities.UserEntity;
import dev.leocamacho.authentication.jpa.repositories.UserRepository;
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
        repository.save(user);
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
