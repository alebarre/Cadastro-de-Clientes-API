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
public class VerificationToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false) private String email;
    @Column(nullable=false) private String code; // 6 dígitos
    @Column(nullable=false) private Instant expiresAt;
    @Column(nullable=false) private boolean used = false;

}
