package com.alebarre.cadastro_clientes.controller;

import com.alebarre.cadastro_clientes.DTO.ForgotRequestDTO;
import com.alebarre.cadastro_clientes.DTO.RegisterRequestDTO;
import com.alebarre.cadastro_clientes.DTO.ResetRequestDTO;
import com.alebarre.cadastro_clientes.DTO.VerifyRequestDTO;
import com.alebarre.cadastro_clientes.service.AuthExtrasService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthExtendedController {
    private final AuthExtrasService svc;
    public AuthExtendedController(AuthExtrasService svc) { this.svc = svc; }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO r) {
        svc.register(r.email(), r.password());
        return ResponseEntity.ok(Map.of("message","Cadastro criado. Verifique seu e-mail."));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequestDTO r) {
        svc.verify(r.email(), r.code());
        return ResponseEntity.ok(Map.of("message","Cadastro verificado. Você já pode entrar."));
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
}

