package dev.leocamacho.authentication.handlers.queries;


import dev.leocamacho.authentication.jpa.entities.UserEntity;
import dev.leocamacho.authentication.jpa.repositories.UserRepository;
import dev.leocamacho.authentication.models.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static dev.leocamacho.authentication.exceptions.BusinessException.exceptionBuilder;

@Service
public class UserAuthenticationQuery {

    @Autowired
    private UserRepository repository;

    public AuthenticatedUser loadUserByUsername(String username) {
        Optional<UserEntity> user = repository.findByEmail(username);

        if (user.isPresent()) {
            return new AuthenticatedUser(
                    user.get().getId(),
                    user.get().getEmail(),
                    user.get().getPassword(),
                    user.get().getRoles()
            );
        } else {
            throw exceptionBuilder().message("User not found with email: " + username).build();
        }
    }
}

