package com.alebarre.cadastro_clientes.service;

import com.alebarre.cadastro_clientes.DTO.AppUserRequestDTO;
import com.alebarre.cadastro_clientes.DTO.AppUserResponseDTO;
import com.alebarre.cadastro_clientes.DTO.AppUserSummaryDTO;
import com.alebarre.cadastro_clientes.domain.AppUser;
import com.alebarre.cadastro_clientes.repository.AppUserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppUserService {
    private static final Set<String> ALLOWED = Set.of("ROLE_ADMIN", "ROLE_USER");

    private final AppUserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    // ===== Helpers de roles (string <-> lista) =====
    private List<String> normalizeRoles(List<String> in) {
        if (in == null || in.isEmpty()) return List.of("ROLE_USER"); // default
        var out = in.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toUpperCase(Locale.ROOT))
                .map(s -> s.startsWith("ROLE_") ? s : "ROLE_" + s)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        // valida
        for (var r : out) {
            if (!ALLOWED.contains(r)) throw new ValidationException("Role inválida: " + r);
        }
        return new ArrayList<>(out);
    }

    private String joinRoles(List<String> roles) {
        return String.join(", ", roles);
    }

    private List<String> splitRoles(String rolesStr) {
        if (rolesStr == null || rolesStr.isBlank()) return List.of();
        return Arrays.stream(rolesStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private AppUserResponseDTO toResponse(AppUser u) {
        return new AppUserResponseDTO(
                u.getId(), u.getUsername(), u.getNome(), u.getEmail(), u.getTelefone(),
                splitRoles(u.getRoles()), u.isEnabled()
        );
    }

    // ===== CRUD =====

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<AppUserSummaryDTO> list() {
        return repo.findAll().stream()
                .map(u -> new AppUserSummaryDTO(
                        u.getId(), u.getUsername(), u.getNome(), u.getEmail(), u.getTelefone(), u.isEnabled()
                ))
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public AppUserResponseDTO find(Long id) {
        var u = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        return toResponse(u);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public AppUserResponseDTO create(AppUserRequestDTO req) {
        if (req.password() == null || req.password().isBlank())
            throw new ValidationException("Senha é obrigatória para novo usuário.");

        if (repo.existsByUsername(req.username()))
            throw new ValidationException("Username já cadastrado.");

        if (repo.existsByEmail(req.email()))
            throw new ValidationException("Email já cadastrado.");

        var u = new AppUser();
        u.setUsername(req.username());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setNome(req.nome());
        u.setEmail(req.email());
        u.setTelefone(req.telefone());
        u.setEnabled(req.enabled() == null || req.enabled());
        u.setRoles(joinRoles(normalizeRoles(req.roles())));

        return toResponse(repo.save(u));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public AppUserResponseDTO update(Long id, AppUserRequestDTO req) {
        var u = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        if (!u.getUsername().equals(req.username()) && repo.existsByUsername(req.username()))
            throw new ValidationException("Username já cadastrado.");

        if (!u.getEmail().equals(req.email()) && repo.existsByEmail(req.email()))
            throw new ValidationException("Email já cadastrado.");

        u.setUsername(req.username());
        if (req.password() != null && !req.password().isBlank())
            u.setPassword(passwordEncoder.encode(req.password()));
        u.setNome(req.nome());
        u.setEmail(req.email());
        u.setTelefone(req.telefone());
        if (req.enabled() != null) u.setEnabled(req.enabled());

        if (req.roles() != null) {
            var roles = normalizeRoles(req.roles());
            var becomingAdmin = roles.contains("ROLE_ADMIN");
            if (!becomingAdmin) {
                long admins = repo.countByRolesContaining("ROLE_ADMIN");
                if (admins <= 1 && splitRoles(u.getRoles()).contains("ROLE_ADMIN"))
                    throw new ValidationException("Não é possível remover o último ADMIN.");
            }
            u.setRoles(joinRoles(roles));
        }

        return toResponse(repo.save(u));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {
        var u = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        // Evitar deletar o último ADMIN
        if (splitRoles(u.getRoles()).contains("ROLE_ADMIN")) {
            long admins = repo.countByRolesContaining("ROLE_ADMIN");
            if (admins <= 1) {
                throw new ValidationException("Não é possível excluir o último ADMIN.");
            }
        }
        repo.delete(u);
    }
}
