package com.alebarre.cadastro_clientes.controller;

import com.alebarre.cadastro_clientes.DTO.*;
import com.alebarre.cadastro_clientes.service.AuthExtrasService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthExtendedController {
    private final AuthExtrasService svc;
    public AuthExtendedController(AuthExtrasService svc) { this.svc = svc; }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequestDTO body) {
        svc.register(body.nome(), body.email(), body.password());
        return ResponseEntity.ok(Map.of("message", "Verifique seu e-mail para confirmar o cadastro."));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequestDTO r) {
        svc.verify(r.email(), r.code());
        return ResponseEntity.ok(Map.of("message","Cadastro verificado. Você já pode entrar."));
    }

    @PostMapping("/resend-verify")
    public ResponseEntity<?> resendVerify(@RequestBody ResendVerifyRequestDTO r) {
        svc.resendVerification(r.email());
        return ResponseEntity.ok(Map.of("message","Código reenviado com sucesso."));
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgot(@RequestBody ForgotRequestDTO r) {
        svc.forgot(r.email());
        return ResponseEntity.ok(Map.of("message","Código enviado para o e-mail informado."));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestBody ResetRequestDTO r) {
        svc.reset(r.email(), r.code(), r.newPassword());
        return ResponseEntity.ok(Map.of("message","Senha redefinida com sucesso."));
    }

    @PostMapping("/resend-reset")
    public ResponseEntity<?> resendReset(@RequestBody ResendResetRequestDTO r) {
        svc.resendReset(r.email());
        return ResponseEntity.ok(Map.of("message","Código reenviado com sucesso."));
    }
}

