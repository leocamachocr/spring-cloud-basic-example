package dev.leocamacho.authentication.api;

import dev.leocamacho.authentication.api.types.RegisterRequest;
import dev.leocamacho.authentication.handlers.commands.RegisterUserHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/public/auth")
public class RegisterUserController {

    @Autowired
    private RegisterUserHandler handler;

    @PostMapping(value = "/register")
    public String register(@RequestBody RegisterRequest request) {
        handler.register(new RegisterUserHandler.Command(
                request.email(),
                request.name(),
                request.password()
        ));

        return "OK";
    }
}
