package com.alebarre.cadastro_clientes.repository;


import com.alebarre.cadastro_clientes.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
}

