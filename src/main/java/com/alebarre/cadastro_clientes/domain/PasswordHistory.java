package com.alebarre.cadastro_clientes.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class PasswordHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private String username;       // email do usuário
    @Column(nullable=false) private String passwordHash;   // hash BCrypt anterior

    @CreationTimestamp
    @Column(nullable=false, updatable=false, columnDefinition = "datetime(6) default CURRENT_TIMESTAMP(6)")
    private Instant createdAt;

}

