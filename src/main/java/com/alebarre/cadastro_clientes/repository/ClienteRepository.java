package com.alebarre.cadastro_clientes.repository;

import com.alebarre.cadastro_clientes.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByEmail(String email);
}
