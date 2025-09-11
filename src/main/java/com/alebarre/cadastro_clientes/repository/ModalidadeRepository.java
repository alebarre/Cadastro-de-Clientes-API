package com.alebarre.cadastro_clientes.repository;

import com.alebarre.cadastro_clientes.domain.Modalidade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModalidadeRepository extends JpaRepository<Modalidade, Long> {}