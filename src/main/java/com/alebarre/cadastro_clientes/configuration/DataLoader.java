package com.alebarre.cadastro_clientes.configuration;

import com.alebarre.cadastro_clientes.domain.AppUser;
import com.alebarre.cadastro_clientes.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {
    @Bean
    CommandLineRunner seedUsers(AppUserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.findByUsername("admin").isEmpty()) {
                AppUser u = new AppUser();
                u.setUsername("admin");
                u.setPassword(encoder.encode("admin123"));
                u.setRoles("ROLE_ADMIN,ROLE_USER");
                repo.save(u);
            }
        };
    }
}

