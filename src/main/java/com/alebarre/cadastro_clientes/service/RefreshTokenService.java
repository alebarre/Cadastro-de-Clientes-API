package com.alebarre.cadastro_clientes.service;

import com.alebarre.cadastro_clientes.domain.RefreshToken;
import com.alebarre.cadastro_clientes.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository repo;
    private final int ttlDays;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository repo,
                               @Value("${app.refresh.ttl-days:7}") int ttlDays) {
        this.repo = repo;
        this.ttlDays = ttlDays;
    }

    public String generateToken() {
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public RefreshToken issue(String username) {
        var rt = new RefreshToken();
        rt.setUsername(username);
        rt.setToken(generateToken());
        rt.setExpiresAt(Instant.now().plus(ttlDays, ChronoUnit.DAYS));
        rt.setRevoked(false);
        return repo.save(rt);
    }

    public RefreshToken rotate(RefreshToken current) {
        current.setRevoked(true);
        repo.save(current);
        return issue(current.getUsername());
    }

    public RefreshToken validateOrThrow(String token) {
        var opt = repo.findByTokenAndRevokedFalse(token);
        var rt = opt.orElseThrow(() -> new IllegalArgumentException("Refresh inválido"));
        if (rt.getExpiresAt().isBefore(Instant.now())) {
            rt.setRevoked(true);
            repo.save(rt);
            throw new IllegalArgumentException("Refresh expirado");
        }
        return rt;
    }

    public void revokeAll(String username) {
        repo.findAll().stream()
                .filter(r -> r.getUsername().equals(username) && !r.isRevoked())
                .forEach(r -> { r.setRevoked(true); repo.save(r); });
    }
}

