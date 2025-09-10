package com.alebarre.cadastro_clientes.controller;

import com.alebarre.cadastro_clientes.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService service;
    public AccountController(AccountService service) { this.service = service; }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal User auth,
                                            @RequestBody ChangePasswordRequest req) {
        service.changePassword(auth.getUsername(), req.currentPassword(), req.newPassword());
        return ResponseEntity.ok(new MessageResponse("Senha alterada com sucesso."));
    }
}

