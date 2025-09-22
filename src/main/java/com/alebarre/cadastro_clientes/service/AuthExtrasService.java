package com.alebarre.cadastro_clientes.service;

import com.alebarre.cadastro_clientes.domain.AppUser;
import com.alebarre.cadastro_clientes.domain.PasswordResetToken;
import com.alebarre.cadastro_clientes.domain.VerificationToken;
import com.alebarre.cadastro_clientes.exception.FieldErrorException;
import com.alebarre.cadastro_clientes.repository.AppUserRepository;
import com.alebarre.cadastro_clientes.repository.PasswordResetTokenRepository;
import com.alebarre.cadastro_clientes.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AuthExtrasService {
    private final AppUserRepository userRepo;
    private final VerificationTokenRepository vRepo;
    private final PasswordResetTokenRepository rRepo;
    private final EmailService mail;
    private final PasswordEncoder encoder;
    private final PasswordPolicyService policy;

    private final int signupTtlMin;
    private final int resetTtlMin;
    private final int resetMaxAttempts;
    private final int cooldownSeconds;

    public AuthExtrasService(
            AppUserRepository userRepo,
            VerificationTokenRepository vRepo,
            PasswordResetTokenRepository rRepo,
            EmailService mail,
            PasswordEncoder encoder,
            PasswordPolicyService policy,
            @Value("${app.signup.code.ttl-min:15}") int signupTtlMin,
            @Value("${app.reset.code.ttl-min:15}") int resetTtlMin,
            @Value("${app.reset.max-attempts:5}") int resetMaxAttempts,
            @Value("${app.code.cooldown-seconds:60}") int cooldownSeconds
    ) {
        this.userRepo = userRepo;
        this.vRepo = vRepo;
        this.rRepo = rRepo;
        this.mail = mail;
        this.encoder = encoder;
        this.policy = policy;
        this.signupTtlMin = signupTtlMin;
        this.resetTtlMin = resetTtlMin;
        this.resetMaxAttempts = resetMaxAttempts;
        this.cooldownSeconds = cooldownSeconds;
    }

    private Instant now() { return Instant.now(); }

    private long secondsSince(Instant createdAt) {
        if (createdAt == null) return Long.MAX_VALUE; // se nulo, considera sem cooldown
        return Duration.between(createdAt, now()).getSeconds();
    }

    private String genCode6() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    // ===== Register =====
    public void register(String email, String rawPassword) {
        // regras de senha → erro de campo "password"
        var ruleErrors = policy.validateRules(rawPassword);
        if (!ruleErrors.isEmpty()) {
            throw new FieldErrorException("Senha inválida", Map.of("password", String.join(" | ", ruleErrors)));
        }

        // e-mail já cadastrado → erro de campo "email"
        if (userRepo.findByUsername(email).isPresent()) {
            throw new FieldErrorException("Email já cadastrado", Map.of("email", "Este e-mail já está em uso"));
        }

        var u = new AppUser();
        u.setUsername(email);
        u.setPassword(encoder.encode(rawPassword));
        u.setRoles("ROLE_USER");
        u.setEnabled(false);
        userRepo.save(u);

        sendVerification(email);
    }

    public void sendVerification(String email) {
        var code = genCode6();
        var tok = new VerificationToken();
        tok.setEmail(email);
        tok.setCode(code);
        tok.setCreatedAt(now()); // garante createdAt
        tok.setExpiresAt(now().plus(signupTtlMin, ChronoUnit.MINUTES));
        tok.setUsed(false);
        vRepo.save(tok);

        mail.sendHtml(email, "Verifique seu cadastro", buildVerificationEmail(code, signupTtlMin));
    }

    // --- RESEND VERIFY ---
    public void resendVerification(String email) {
        userRepo.findByUsername(email).orElseThrow(() ->
                new FieldErrorException("Email não cadastrado", Map.of("email", "E-mail não localizado"))
        );

        var last = vRepo.findTopByEmailAndUsedFalseOrderByIdDesc(email).orElse(null);
        if (last != null) {
            long since = secondsSince(last.getCreatedAt());
            if (since < cooldownSeconds) {
                long remaining = cooldownSeconds - since;
                throw tooManyRequests("Aguarde " + remaining + "s para reenviar.");
            }
            // invalida o token anterior para não deixar dois válidos
            last.setUsed(true);
            vRepo.save(last);
        }

        sendVerification(email);
    }

    // --- RESEND RESET ---
    public void resendReset(String email) {
        userRepo.findByUsername(email).orElseThrow(() ->
                new FieldErrorException("Email não cadastrado", Map.of("email", "E-mail não localizado"))
        );

        var last = rRepo.findTopByEmailAndUsedFalseOrderByIdDesc(email).orElse(null);
        if (last != null) {
            long since = secondsSince(last.getCreatedAt());
            if (since < cooldownSeconds) {
                long remaining = cooldownSeconds - since;
                throw tooManyRequests("Aguarde " + remaining + "s para reenviar.");
            }
            last.setUsed(true);
            rRepo.save(last);
        }

        forgot(email); // reaproveita fluxo que já cria e manda email
    }

    public void verify(String email, String code) {
        var tok = vRepo.findTopByEmailAndUsedFalseOrderByIdDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("Código não solicitado"));

        if (tok.isUsed() || tok.getExpiresAt().isBefore(now()) || !tok.getCode().equals(code)) {
            throw new IllegalArgumentException("Código inválido ou expirado");
        }

        var u = userRepo.findByUsername(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        u.setEnabled(true);
        userRepo.save(u);
        tok.setUsed(true);
        vRepo.save(tok);
    }

    // ===== Forgot / Reset =====
    public void forgot(String email) {
        userRepo.findByUsername(email).orElseThrow(() ->
                new FieldErrorException("Email não cadastrado", Map.of("email", "E-mail não localizado"))
        );

        var code = genCode6();
        var tok = new PasswordResetToken();
        tok.setEmail(email);
        tok.setCode(code);
        tok.setCreatedAt(now()); // garante createdAt
        tok.setExpiresAt(now().plus(resetTtlMin, ChronoUnit.MINUTES));
        tok.setUsed(false);
        tok.setAttempts(0);
        rRepo.save(tok);

        mail.sendHtml(email, "Redefinição de senha", buildResetEmail(code, resetTtlMin));
    }

    public void reset(String email, String code, String newPassword) {
        // regras + histórico → erro de campo "newPassword"
        var ruleErrors = policy.validateRules(newPassword);
        var historyErrors = policy.validateHistory(email, newPassword);
        var all = new ArrayList<String>(); all.addAll(ruleErrors); all.addAll(historyErrors);
        if (!all.isEmpty()) {
            throw new FieldErrorException("Senha inválida", Map.of("newPassword", String.join(" | ", all)));
        }

        var tok = rRepo.findTopByEmailAndUsedFalseOrderByIdDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("Código não solicitado"));

        // valida código/expiração
        boolean invalid = tok.isUsed()
                || tok.getExpiresAt().isBefore(now())
                || !Objects.equals(tok.getCode(), code);

        if (invalid) {
            tok.setAttempts(tok.getAttempts() + 1);
            if (tok.getAttempts() >= resetMaxAttempts) {
                tok.setUsed(true);
            }
            rRepo.save(tok); // <- salva attempts/used atualizados
            throw new IllegalArgumentException("Código inválido ou expirado");
        }

        var u = userRepo.findByUsername(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        // grava histórico ANTES de trocar
        policy.record(email, u.getPassword());
        u.setPassword(encoder.encode(newPassword));
        userRepo.save(u);

        tok.setUsed(true);
        rRepo.save(tok);
    }

    private String buildVerificationEmail(String code, int ttlMin) {
        return """
    <html>
      <body style="font-family: Arial, sans-serif; color: #333;">
        <h2 style="color:#2c3e50;">Confirme seu cadastro</h2>
        <p>Use o código abaixo para ativar sua conta:</p>
        <div style="font-size: 24px; font-weight: bold; 
                    color: white; background:#3498db; 
                    padding:10px 20px; display:inline-block; border-radius:5px;">
          %s
        </div>
        <p style="margin-top:20px;">Este código expira em <strong>%d minutos</strong>.</p>
        <hr/>
        <p style="font-size:12px;color:#888;">Se você não solicitou este cadastro, ignore este email.</p>
      </body>
    </html>
    """.formatted(code, ttlMin);
    }

    private String buildResetEmail(String code, int ttlMin) {
        return """
    <html>
      <body style="font-family: Arial, sans-serif; color: #333;">
        <h2 style="color:#e74c3c;">Redefinição de senha</h2>
        <p>Você solicitou a redefinição da sua senha. Use o código abaixo:</p>
        <div style="font-size: 24px; font-weight: bold; 
                    color: white; background:#e67e22; 
                    padding:10px 20px; display:inline-block; border-radius:5px;">
          %s
        </div>
        <p style="margin-top:20px;">Este código expira em <strong>%d minutos</strong>.</p>
        <p>Se não foi você que solicitou, apenas ignore este e-mail.</p>
      </body>
    </html>
    """.formatted(code, ttlMin);
    }

    private RuntimeException tooManyRequests(String msg) {
        return new TooManyRequestsException(msg);
    }

    // exception simples -> mapeada para 429 no ApiExceptionHandler
    public static class TooManyRequestsException extends RuntimeException {
        public TooManyRequestsException(String m) { super(m); }
    }
}
