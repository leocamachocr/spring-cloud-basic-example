package dev.leocamacho.authentication.exceptions;


import java.util.UUID;

public record ErrorResponse(
        String message,
        int code,
        UUID correlationId
) {

}