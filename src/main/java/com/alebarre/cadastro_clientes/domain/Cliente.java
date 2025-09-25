package com.alebarre.cadastro_clientes.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Cliente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min = 3, max = 120)
    private String nome;

    @NotBlank @Email @Column(unique = true)
    private String email;

    private String telefone;

    private String cpf; // opcional

    @Past(message = "Data de nascimento deve estar no passado")
    private LocalDate dataNascimento;

    @Column(unique = true)
    private boolean enabled;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Endereco> enderecos = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "cliente_modalidade",
            joinColumns = @JoinColumn(name = "cliente_id"),
            inverseJoinColumns = @JoinColumn(name = "modalidade_id")
    )
    private Set<Modalidade> modalidades = new HashSet<>();

    public void setEnderecos(List<Endereco> novos) {
        this.enderecos.clear();
        if (novos != null) {
            novos.forEach(this::addEndereco);
        }
    }

    public void addEndereco(Endereco e) {
        e.setCliente(this);
        this.enderecos.add(e);
    }
}