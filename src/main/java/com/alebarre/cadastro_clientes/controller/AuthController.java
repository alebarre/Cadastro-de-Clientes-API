package com.alebarre.cadastro_clientes.controller;


import com.alebarre.cadastro_clientes.DTO.AuthRequestDTO;
import com.alebarre.cadastro_clientes.DTO.AuthResponseDTO;
import com.alebarre.cadastro_clientes.DTO.RefreshRequestDTO;
import com.alebarre.cadastro_clientes.DTO.RefreshResponseDTO;
import com.alebarre.cadastro_clientes.security.JWTService;
import com.alebarre.cadastro_clientes.service.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final UserDetailsService uds;
    private final JWTService jwt;
    private final RefreshTokenService rts;

    public AuthController(AuthenticationManager am, UserDetailsService uds, JWTService jwt, RefreshTokenService rts) {
        this.authManager = am; this.uds = uds; this.jwt = jwt; this.rts = rts;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO req) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
            var user = uds.loadUserByUsername(req.username());
            String access = jwt.generateToken(user.getUsername(),
                    String.join(",", user.getAuthorities().stream().map(a -> a.getAuthority()).toList()));
            long expiresIn = jwt.getExpirationSeconds(); // <—
            var rt = rts.issue(user.getUsername());
            return ResponseEntity.ok(new AuthResponseDTO(rt.getToken(), access, expiresIn, user.getUsername()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", 401, "error","Unauthorized", "message","Credenciais inválidas", "timestamp", Instant.now().toString()
            ));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequestDTO req) {
        try {
            var current = rts.validateOrThrow(req.refreshToken());
            var user = uds.loadUserByUsername(current.getUsername());
            String access = jwt.generateToken(user.getUsername(),
                    String.join(",", user.getAuthorities().stream().map(a -> a.getAuthority()).toList()));
            long expiresIn = jwt.getExpirationSeconds(); // <—
            var rotated = rts.rotate(current);
            return ResponseEntity.ok(new RefreshResponseDTO(access, expiresIn, rotated.getToken()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", 401, "error", "Unauthorized", "message", "Refresh inválido ou expirado", "timestamp", Instant.now().toString()
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshRequestDTO req) {
        try {
            var current = rts.validateOrThrow(req.refreshToken());
            current.setRevoked(true);
            rts.rotate(current); // opcional: apenas revoga; rotate para invalidação imediata
        } catch (Exception ignored) { }
        return ResponseEntity.ok(Map.of("message","Logout efetuado"));
    }

}

