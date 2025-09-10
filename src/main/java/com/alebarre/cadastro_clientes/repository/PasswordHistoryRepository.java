package com.alebarre.cadastro_clientes.repository;

import com.alebarre.cadastro_clientes.domain.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    List<PasswordHistory> findTop5ByUsernameOrderByCreatedAtDesc(String username);
    // o "5" será o máximo, e conforme app.password.history-size
}