package com.alebarre.cadastro_clientes.security;

import com.alebarre.cadastro_clientes.repository.AppUserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final AppUserRepository repo;
    public AppUserDetailsService(AppUserRepository repo) { this.repo = repo; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var u = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        var auths = org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList(u.getRoles());
        return new org.springframework.security.core.userdetails.User(u.getUsername(), u.getPassword(), auths);
    }
}

