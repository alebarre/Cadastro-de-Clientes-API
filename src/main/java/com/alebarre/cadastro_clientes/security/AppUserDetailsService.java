package com.alebarre.cadastro_clientes.security;

import com.alebarre.cadastro_clientes.repository.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final AppUserRepository repo;

    public AppUserDetailsService(AppUserRepository repo) { this.repo = repo; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var u = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        var authorities = toAuthorities(u.getRoles());

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getUsername())
                .password(u.getPassword())
                .authorities(authorities)
                .disabled(!u.isEnabled())      // reflete o enabled do banco
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }

    private static List<SimpleGrantedAuthority> toAuthorities(String roles) {
        if (roles == null || roles.isBlank()) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .toList(); // se estiver em Java 11, troque por .collect(Collectors.toList())
    }
}


