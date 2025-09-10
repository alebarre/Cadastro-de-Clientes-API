package com.alebarre.cadastro_clientes.service;

import com.alebarre.cadastro_clientes.domain.AppUser;
import com.alebarre.cadastro_clientes.domain.PasswordResetToken;
import com.alebarre.cadastro_clientes.domain.VerificationToken;
import com.alebarre.cadastro_clientes.exception.ApiExceptionHandler;
import com.alebarre.cadastro_clientes.repository.AppUserRepository;
import com.alebarre.cadastro_clientes.repository.PasswordResetTokenRepository;
import com.alebarre.cadastro_clientes.repository.VerificationTokenRepository;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

@Service
public class AuthExtrasService {
    private final AppUserRepository userRepo;
    private final VerificationTokenRepository vRepo;
    private final PasswordResetTokenRepository rRepo;
    private final EmailService mail;
    private final PasswordEncoder encoder;
    private final ApiExceptionHandler apiExceptionHandler;

    private final int signupTtlMin;
    private final int resetTtlMin;
    private final int resetMaxAttempts;

    private final int cooldownSeconds;

    private final PasswordPolicyService policy;

    public AuthExtrasService(
            AppUserRepository userRepo, VerificationTokenRepository vRepo, PasswordResetTokenRepository rRepo,
            EmailService mail, PasswordEncoder encoder, ApiExceptionHandler apiExceptionHandler,
            @Value("${app.signup.code.ttl-min:15}") int signupTtlMin,
            @Value("${app.reset.code.ttl-min:15}") int resetTtlMin,
            @Value("${app.reset.max-attempts:5}") int resetMaxAttempts,
            @Value("${app.code.cooldown-seconds:60}") int cooldownSeconds, PasswordPolicyService policy
    ) {
        this.userRepo = userRepo; this.vRepo = vRepo; this.rRepo = rRepo;
        this.mail = mail; this.encoder = encoder;
        this.apiExceptionHandler = apiExceptionHandler;
        this.signupTtlMin = signupTtlMin;
        this.resetTtlMin = resetTtlMin;
        this.resetMaxAttempts = resetMaxAttempts;
        this.cooldownSeconds = cooldownSeconds;
        this.policy = policy;
    }

    private String genCode6() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    // ===== Register =====
    public void register(String email, String rawPassword) {
        var ruleErrors = policy.validateRules(rawPassword);
        if (!ruleErrors.isEmpty()) {
            throw apiExceptionHandler.validation("Senha inválida", Map.of("password", String.join(" | ", ruleErrors)));
        }
        if (userRepo.findByUsername(email).isPresent())
            throw new ValidationException("Email já cadastrado");

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
        tok.setExpiresAt(Instant.now().plus(signupTtlMin, ChronoUnit.MINUTES));
        vRepo.save(tok);

        mail.sendHtml(email, "Verifique seu cadastro", buildVerificationEmail(code, signupTtlMin));
    }

    // --- RESEND VERIFY ---
    public void resendVerification(String email) {
        // precisa existir o usuário (pode estar disabled)
        userRepo.findByUsername(email).orElseThrow(() -> new jakarta.validation.ValidationException("Email não cadastrado"));

        var last = vRepo.findTopByEmailAndUsedFalseOrderByIdDesc(email).orElse(null);
        if (last != null) {
            long since = java.time.Duration.between(last.getCreatedAt(), Instant.now()).getSeconds();
            if (since < cooldownSeconds) {
                long remaining = cooldownSeconds - since;
                throw tooManyRequests("Aguarde " + remaining + "s para reenviar.");
            }
            // invalida o token anterior para não deixar dois válidos
            last.setUsed(true);
            vRepo.save(last);
        }
        // emite novo token e envia
        sendVerification(email);
    }

    // --- RESEND RESET ---
    public void resendReset(String email) {
        userRepo.findByUsername(email).orElseThrow(() -> new jakarta.validation.ValidationException("Email não cadastrado"));

        var last = rRepo.findTopByEmailAndUsedFalseOrderByIdDesc(email).orElse(null);
        if (last != null) {
            long since = java.time.Duration.between(last.getCreatedAt(), Instant.now()).getSeconds();
            if (since < cooldownSeconds) {
                long remaining = cooldownSeconds - since;
                throw tooManyRequests("Aguarde " + remaining + "s para reenviar.");
            }
            last.setUsed(true);
            rRepo.save(last);
        }
        // emite novo token e envia
        forgot(email); // reaproveita fluxo que já cria e manda email
    }

    public void verify(String email, String code) {
        var tok = vRepo.findTopByEmailAndUsedFalseOrderByIdDesc(email)
                .orElseThrow(() -> new ValidationException("Código não solicitado"));

        if (tok.isUsed() || tok.getExpiresAt().isBefore(Instant.now()) || !tok.getCode().equals(code))
            throw new ValidationException("Código inválido ou expirado");

        var u = userRepo.findByUsername(email)
                .orElseThrow(() -> new ValidationException("Usuário não encontrado"));

        u.setEnabled(true);
        userRepo.save(u);
        tok.setUsed(true);
        vRepo.save(tok);
    }

    // ===== Forgot / Reset =====
    public void forgot(String email) {
        userRepo.findByUsername(email).orElseThrow(() -> new ValidationException("Email não cadastrado"));

        var code = genCode6();
        var tok = new PasswordResetToken();
        tok.setEmail(email);
        tok.setCode(code);
        tok.setExpiresAt(Instant.now().plus(resetTtlMin, ChronoUnit.MINUTES));
        rRepo.save(tok);

        mail.sendHtml(email, "Redefinição de senha", buildResetEmail(code, resetTtlMin));
    }

    public void reset(String email, String code, String newPassword) {
        // ... valida código ...
        var ruleErrors = policy.validateRules(newPassword);
        var historyErrors = policy.validateHistory(email, newPassword);
        var all = new ArrayList<String>(); all.addAll(ruleErrors); all.addAll(historyErrors);
        if (!all.isEmpty()) {
            throw apiExceptionHandler.validation("Senha inválida", Map.of("newPassword", String.join(" | ", all)));
        }
        var tok = rRepo.findTopByEmailAndUsedFalseOrderByIdDesc(email)
                .orElseThrow(() -> new ValidationException("Código não solicitado"));

        if (tok.isUsed() || tok.getExpiresAt().isBefore(Instant.now()) || !tok.getCode().equals(code)) {
            tok.setAttempts(tok.getAttempts()+1);
            rRepo.save(tok);
            if (tok.getAttempts() >= resetMaxAttempts) tok.setUsed(true);
            throw new ValidationException("Código inválido ou expirado");
        }

        var u = userRepo.findByUsername(email)
                .orElseThrow(() -> new ValidationException("Usuário não encontrado"));
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
        // você pode ter um @ControllerAdvice para converter isso em HTTP 429
        return new TooManyRequestsException(msg);
    }

    // exception simples
    public static class TooManyRequestsException extends RuntimeException {
        public TooManyRequestsException(String m) { super(m); }
    }

}

