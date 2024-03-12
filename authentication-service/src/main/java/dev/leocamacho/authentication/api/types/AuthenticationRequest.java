package dev.leocamacho.authentication.api.types;


public record AuthenticationRequest(
        String email,
        String password
) {
}