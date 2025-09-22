package com.alebarre.cadastro_clientes.exception;

import com.alebarre.cadastro_clientes.service.AuthExtrasService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    record Problem(int status, String error, String message, Instant timestamp, Map<String,String> fieldErrors) {}

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Problem> notFound(EntityNotFoundException ex) {
        var body = new Problem(404, "Not Found", ex.getMessage(), Instant.now(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // ⬇️ NOVO: erros de negócio com fieldErrors (ex.: trocar senha igual às últimas 5)
    @ExceptionHandler(FieldErrorException.class)
    public ResponseEntity<Problem> onFieldError(FieldErrorException ex) {
        var body = new Problem(400, "Validation Error", ex.getMessage(), Instant.now(), ex.getFieldErrors());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> beanValidation(MethodArgumentNotValidException ex) {
        Map<String,String> fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        err -> err.getField(),
                        err -> Optional.ofNullable(err.getDefaultMessage()).orElse("Inválido"),
                        (a,b) -> a
                ));
        var body = new Problem(400, "Validation Error", "Campos inválidos", Instant.now(), fields);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Problem> constraint(ConstraintViolationException ex) {
        Map<String,String> fields = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage(),
                        (a,b) -> a
                ));
        var body = new Problem(400, "Validation Error", "Campos inválidos", Instant.now(), fields);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AuthExtrasService.TooManyRequestsException.class)
    public ResponseEntity<?> handleTooMany(AuthExtrasService.TooManyRequestsException ex) {
        return ResponseEntity.status(429).body(Map.of(
                "status", 429, "error", "Too Many Requests",
                "message", ex.getMessage(), "timestamp", Instant.now().toString()
        ));
    }

    // (Opcional) catch-all para outras RuntimeException de validação sem field map
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Problem> illegalArg(IllegalArgumentException ex) {
        var body = new Problem(400, "Validation Error", ex.getMessage(), Instant.now(), null);
        return ResponseEntity.badRequest().body(body);
    }
}
