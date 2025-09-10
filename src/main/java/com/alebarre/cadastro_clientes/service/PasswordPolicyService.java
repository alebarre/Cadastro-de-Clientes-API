package com.alebarre.cadastro_clientes.service;

import com.alebarre.cadastro_clientes.domain.PasswordHistory;
import com.alebarre.cadastro_clientes.repository.PasswordHistoryRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;

import java.util.*;

@Service
public class PasswordPolicyService {
    private final int minLength;
    private final boolean requireUpper, requireLower, requireDigit, requireSpecial, disallowCommon;
    private final int historySize;
    private final PasswordHistoryRepository historyRepo;
    private final PasswordEncoder encoder;

    private static final Set<String> COMMON = Set.of(
            "123456","12345678","123456789","12345","qwerty","111111","123123","000000","password","senha","admin"
    );

    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    public PasswordPolicyService(
            @Value("${app.password.min-length:8}") int minLength,
            @Value("${app.password.require-upper:true}") boolean requireUpper,
            @Value("${app.password.require-lower:true}") boolean requireLower,
            @Value("${app.password.require-digit:true}") boolean requireDigit,
            @Value("${app.password.require-special:true}") boolean requireSpecial,
            @Value("${app.password.disallow-common:true}") boolean disallowCommon,
            @Value("${app.password.history-size:5}") int historySize,
            PasswordHistoryRepository historyRepo,
            PasswordEncoder encoder
    ) {
        this.minLength = minLength;
        this.requireUpper = requireUpper;
        this.requireLower = requireLower;
        this.requireDigit = requireDigit;
        this.requireSpecial = requireSpecial;
        this.disallowCommon = disallowCommon;
        this.historySize = historySize;
        this.historyRepo = historyRepo;
        this.encoder = encoder;
    }

    /** Retorna lista de mensagens de erro; vazia = válido */
    public List<String> validateRules(String raw) {
        List<String> errors = new ArrayList<>();
        if (raw == null || raw.length() < minLength)
            errors.add("Mínimo de " + minLength + " caracteres");
        if (requireUpper && !UPPER.matcher(raw).find())
            errors.add("Ao menos 1 letra maiúscula");
        if (requireLower && !LOWER.matcher(raw).find())
            errors.add("Ao menos 1 letra minúscula");
        if (requireDigit && !DIGIT.matcher(raw).find())
            errors.add("Ao menos 1 dígito");
        if (requireSpecial && !SPECIAL.matcher(raw).find())
            errors.add("Ao menos 1 caractere especial");
        if (disallowCommon && COMMON.contains(raw.toLowerCase()))
            errors.add("Evite senhas comuns");
        return errors;
    }

    public List<String> validateHistory(String username, String newRaw) {
        if (historySize <= 0) return List.of();
        var last = historyRepo.findTop5ByUsernameOrderByCreatedAtDesc(username);
        List<String> errors = new ArrayList<>();
        int checked = 0;
        for (PasswordHistory h : last) {
            if (checked >= historySize) break;
            if (encoder.matches(newRaw, h.getPasswordHash())) {
                errors.add("Não reutilize uma das últimas " + historySize + " senhas");
                break;
            }
            checked++;
        }
        return errors;
    }

    /** Salva hash no histórico e limita tamanho */
    public void record(String username, String passwordHash) {
        var ph = new PasswordHistory();
        ph.setUsername(username);
        ph.setPasswordHash(passwordHash);
        historyRepo.save(ph);

        // manter no máx 'historySize' registros
        var all = historyRepo.findTop5ByUsernameOrderByCreatedAtDesc(username);
        if (all.size() > historySize) {
            all.stream().skip(historySize).forEach(historyRepo::delete);
        }
    }
}

