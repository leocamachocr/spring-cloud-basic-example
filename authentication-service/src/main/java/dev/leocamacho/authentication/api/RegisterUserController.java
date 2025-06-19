package dev.leocamacho.authentication.api;

import dev.leocamacho.authentication.api.types.RegisterRequest;
import dev.leocamacho.authentication.handlers.commands.RegisterUserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/public/auth")
public class RegisterUserController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterUserController.class);

    @Autowired
    private RegisterUserHandler handler;

    @PostMapping(value = "/register")
    public String register(@RequestBody RegisterRequest request) {
        logger.info("Registering user: {}", request.email());
        handler.register(new RegisterUserHandler.Command(
                request.email(),
                request.name(),
                request.password()
        ));

        return "OK";
    }
}
