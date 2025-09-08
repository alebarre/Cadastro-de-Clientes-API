package com.alebarre.cadastro_clientes.controller;

import com.alebarre.cadastro_clientes.security.JWTService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
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

    public AuthController(AuthenticationManager am, UserDetailsService uds, JWTService jwt) {
        this.authManager = am; this.uds = uds; this.jwt = jwt;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
            UserDetails user = uds.loadUserByUsername(req.username());
            String token = jwt.generateToken(user.getUsername(),
                    String.join(",", user.getAuthorities().stream().map(a -> a.getAuthority()).toList()));
            long expiresIn = 3600; // seg (igual ao jwt.expiration-ms/1000)
            return ResponseEntity.ok(new AuthResponse(token, expiresIn, user.getUsername()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", 401,
                    "error", "Unauthorized",
                    "message", "Credenciais inválidas",
                    "timestamp", Instant.now().toString()
            ));
        }
    }
}

