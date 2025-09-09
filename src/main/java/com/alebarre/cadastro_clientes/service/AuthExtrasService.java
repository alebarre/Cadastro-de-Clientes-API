package com.alebarre.cadastro_clientes.service;

import com.alebarre.cadastro_clientes.domain.AppUser;
import com.alebarre.cadastro_clientes.domain.PasswordResetToken;
import com.alebarre.cadastro_clientes.domain.VerificationToken;
import com.alebarre.cadastro_clientes.repository.AppUserRepository;
import com.alebarre.cadastro_clientes.repository.PasswordResetTokenRepository;
import com.alebarre.cadastro_clientes.repository.VerificationTokenRepository;
import jakarta.validation.ValidationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
public class AuthExtrasService {
    private final AppUserRepository userRepo;
    private final VerificationTokenRepository vRepo;
    private final PasswordResetTokenRepository rRepo;
    private final EmailService mail;
    private final PasswordEncoder encoder;

    private final int signupTtlMin;
    private final int resetTtlMin;
    private final int resetMaxAttempts;

    public AuthExtrasService(
            AppUserRepository userRepo, VerificationTokenRepository vRepo, PasswordResetTokenRepository rRepo,
            EmailService mail, PasswordEncoder encoder,
            @org.springframework.beans.factory.annotation.Value("${app.signup.code.ttl-min:15}") int signupTtlMin,
            @org.springframework.beans.factory.annotation.Value("${app.reset.code.ttl-min:15}") int resetTtlMin,
            @org.springframework.beans.factory.annotation.Value("${app.reset.max-attempts:5}") int resetMaxAttempts
    ) {
        this.userRepo = userRepo; this.vRepo = vRepo; this.rRepo = rRepo;
        this.mail = mail; this.encoder = encoder;
        this.signupTtlMin = signupTtlMin;
        this.resetTtlMin = resetTtlMin;
        this.resetMaxAttempts = resetMaxAttempts;
    }

    private String genCode6() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    // ===== Register =====
    public void register(String email, String rawPassword) {
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

}

