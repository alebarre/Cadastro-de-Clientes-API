package com.alebarre.cadastro_clientes.repository;

import com.alebarre.cadastro_clientes.domain.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findTopByEmailAndUsedFalseOrderByIdDesc(String email);
}
