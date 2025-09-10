package com.alebarre.cadastro_clientes.service;

import com.alebarre.cadastro_clientes.domain.AppUser;
import com.alebarre.cadastro_clientes.exception.FieldErrorException;
import com.alebarre.cadastro_clientes.repository.AppUserRepository;
import jakarta.validation.ValidationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final PasswordPolicyService policy;
    private final RefreshTokenService refreshTokens;

    public AccountService(AppUserRepository users, PasswordEncoder encoder,
                          PasswordPolicyService policy, RefreshTokenService refreshTokens) {
        this.users = users;
        this.encoder = encoder;
        this.policy = policy;
        this.refreshTokens = refreshTokens;
    }

    public void changePassword(String username, String currentRaw, String newRaw) {
        AppUser u = users.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Usuário não encontrado"));

        // valida senha atual
        if (currentRaw == null || !encoder.matches(currentRaw, u.getPassword())) {
            throw FieldErrorException.of("Credenciais inválidas", "currentPassword", "Senha atual incorreta");
        }

        // política e histórico
        var errors = new java.util.ArrayList<String>();
        errors.addAll(policy.validateRules(newRaw));
        errors.addAll(policy.validateHistory(username, newRaw));
        if (!errors.isEmpty()) {
            throw FieldErrorException.of("Senha inválida", "newPassword", String.join(" | ", errors));
        }

        // grava histórico ANTES de trocar
        policy.record(username, u.getPassword());

        // troca a senha
        u.setPassword(encoder.encode(newRaw));
        users.save(u);

        // revogar todos os refresh tokens do usuário (forçar re-login)
        refreshTokens.revokeAll(username);
    }
}

