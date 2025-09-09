package com.alebarre.cadastro_clientes.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String token; // opaco e aleatório (rotacionado)

    @Column(nullable=false)
    private String username;

    @Column(nullable=false)
    private Instant expiresAt;

    @Column(nullable=false)
    private boolean revoked = false;

}
