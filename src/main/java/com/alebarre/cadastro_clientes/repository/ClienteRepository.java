package com.alebarre.cadastro_clientes.repository;

import com.alebarre.cadastro_clientes.domain.Cliente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"enderecos", "modalidades"})
    List<Cliente> findAll();
}
