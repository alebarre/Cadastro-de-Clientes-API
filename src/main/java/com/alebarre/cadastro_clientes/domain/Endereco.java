package com.alebarre.cadastro_clientes.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Endereco {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank private String logradouro;
    @NotBlank private String numero;
    private String complemento;
    @NotBlank private String bairro;
    @NotBlank private String cidade;
    @NotBlank @Size(min=2,max=2) private String uf;
    @NotBlank @Pattern(regexp="\\d{8}") private String cep; // 8 dígitos
    String Pais;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

}
