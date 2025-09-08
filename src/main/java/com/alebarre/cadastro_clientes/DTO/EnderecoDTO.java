package com.alebarre.cadastro_clientes.DTO;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

public record EnderecoDTO(
        Long id,
        @NotBlank String logradouro,
        @NotBlank String numero,
        String complemento,
        @NotBlank String bairro,
        @NotBlank String cidade,
        @NotBlank @Size(min=2,max=2) String uf,
        @NotBlank @Pattern(regexp="\\d{8}") String cep
) {}
