package com.alebarre.cadastro_clientes.exception;

import com.alebarre.cadastro_clientes.service.AuthExtrasService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {

    record Problem(int status, String error, String message, Instant timestamp, Map<String,String> fieldErrors) {}

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Problem> notFound(EntityNotFoundException ex) {
        var body = new Problem(404, "Not Found", ex.getMessage(), Instant.now(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Problem> validation(ValidationException ex) {
        var body = new Problem(400, "Validation Error", ex.getMessage(), Instant.now(), null);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> beanValidation(MethodArgumentNotValidException ex) {
        Map<String,String> fields = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> fields.put(err.getField(), err.getDefaultMessage()));
        var body = new Problem(400, "Validation Error", "Campos inválidos", Instant.now(), fields);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AuthExtrasService.TooManyRequestsException.class)
    public ResponseEntity<?> handleTooMany(AuthExtrasService.TooManyRequestsException ex) {
        return ResponseEntity.status(429).body(Map.of(
                "status", 429, "error", "Too Many Requests", "message", ex.getMessage(),
                "timestamp", java.time.Instant.now().toString()
        ));
    }
}
