package com.alebarre.cadastro_clientes.domain;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Modalidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String nome;

    private String descricao;

    @Column(precision=10, scale=2, nullable=false)
    private BigDecimal valor;

    @ManyToMany(mappedBy = "modalidades")
    private Set<Cliente> clientes = new HashSet<>();
}
