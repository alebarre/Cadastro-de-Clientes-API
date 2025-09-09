package com.alebarre.cadastro_clientes.controller;

import com.alebarre.cadastro_clientes.service.AuthExtrasService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

record RegisterRequest(@NotBlank @Email String email,
                       @NotBlank @Size(min=6,max=100) String password) {}
record VerifyRequest(@NotBlank @Email String email,
                     @NotBlank @Size(min=6,max=6) String code) {}
record ForgotRequest(@NotBlank @Email String email) {}
record ResetRequest(@NotBlank @Email String email,
                    @NotBlank @Size(min=6,max=6) String code,
                    @NotBlank @Size(min=6,max=100) String newPassword) {}

@RestController
@RequestMapping("/api/auth")
public class AuthExtendedController {
    private final AuthExtrasService svc;
    public AuthExtendedController(AuthExtrasService svc) { this.svc = svc; }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest r) {
        svc.register(r.email(), r.password());
        return ResponseEntity.ok(Map.of("message","Cadastro criado. Verifique seu e-mail."));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest r) {
        svc.verify(r.email(), r.code());
        return ResponseEntity.ok(Map.of("message","Cadastro verificado. Você já pode entrar."));
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgot(@RequestBody ForgotRequest r) {
        svc.forgot(r.email());
        return ResponseEntity.ok(Map.of("message","Código enviado para o e-mail informado."));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestBody ResetRequest r) {
        svc.reset(r.email(), r.code(), r.newPassword());
        return ResponseEntity.ok(Map.of("message","Senha redefinida com sucesso."));
    }
}

