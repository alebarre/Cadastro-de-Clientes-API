package com.alebarre.cadastro_clientes.repository;

import com.alebarre.cadastro_clientes.domain.Modalidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface ModalidadeRepository extends JpaRepository<Modalidade, Long> {
    long countByIdIn(Set<Long> ids);
}