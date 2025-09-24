package com.alebarre.cadastro_clientes.controller;


import com.alebarre.cadastro_clientes.DTO.AuthRequestDTO;
import com.alebarre.cadastro_clientes.DTO.AuthResponseDTO;
import com.alebarre.cadastro_clientes.DTO.RefreshRequestDTO;
import com.alebarre.cadastro_clientes.DTO.RefreshResponseDTO;
import com.alebarre.cadastro_clientes.exception.TooManyLoginAttemptsException;
import com.alebarre.cadastro_clientes.security.JWTService;
import com.alebarre.cadastro_clientes.service.RefreshTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final UserDetailsService uds;
    private final JWTService jwt;
    private final RefreshTokenService rts;
    private int attemptsCount = 0;

    public AuthController(AuthenticationManager am, UserDetailsService uds, JWTService jwt, RefreshTokenService rts) {
        this.authManager = am; this.uds = uds; this.jwt = jwt; this.rts = rts;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO req) {
        try {
            // Autentica e usa o principal retornado pelo provider (respeita enabled/locked/expired)
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );
            UserDetails user = (UserDetails) auth.getPrincipal();

            // Gera tokens com as authorities do authentication
            String roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
            String access = jwt.generateToken(user.getUsername(), roles);
            long expiresIn = jwt.getExpirationSeconds();
            var rt = rts.issue(user.getUsername());

            attemptsCount = 0; // zera contador em sucesso
            return ResponseEntity.ok(new AuthResponseDTO(
                    rt.getToken(), access, expiresIn, user.getUsername()
            ));

        }  catch (BadCredentialsException e) {
            // Credenciais inválidas → 401
            attemptsCount++;
            if (attemptsCount >= 10) { attemptsCount = 0; throw new TooManyLoginAttemptsException(60); }

            var pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
            pd.setTitle("Unauthorized");
            pd.setDetail("Credenciais inválidas");
            pd.setProperty("path", "/api/auth/login");
            pd.setProperty("timestamp", Instant.now().toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(pd);

        } catch (DisabledException e) {
            // Conta desabilitada → 403
            attemptsCount++;
            if (attemptsCount >= 10) { attemptsCount = 0; throw new TooManyLoginAttemptsException(60); }

            var pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
            pd.setTitle("Conta inativa");
            pd.setDetail("Seu usuário está inativo. Contate o administrador.");
            pd.setProperty("path", "/api/auth/login");
            pd.setProperty("timestamp", Instant.now().toString());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(pd);

        } catch (AccountStatusException e) {
            // Outras irregularidades de conta (locked/expired/credentialsExpired) → 403
            attemptsCount++;
            if (attemptsCount >= 10) { attemptsCount = 0; throw new TooManyLoginAttemptsException(60); }

            var pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
            pd.setTitle("Conta com restrições");
            pd.setDetail("Sua conta não está apta a autenticar.");
            pd.setProperty("path", "/api/auth/login");
            pd.setProperty("timestamp", Instant.now().toString());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(pd);
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

