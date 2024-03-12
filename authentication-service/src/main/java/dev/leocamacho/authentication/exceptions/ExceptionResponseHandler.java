package dev.leocamacho.authentication.exceptions;

import dev.leocamacho.authentication.session.SessionContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionResponseHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {

        ErrorResponse response = new ErrorResponse(
                ex.getMessage(),
                ex.getCode(),
                SessionContextHolder.getSession().correlationId()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
