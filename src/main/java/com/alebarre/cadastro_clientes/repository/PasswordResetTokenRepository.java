package com.alebarre.cadastro_clientes.repository;

import com.alebarre.cadastro_clientes.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findTopByEmailAndUsedFalseOrderByIdDesc(String email);
}

