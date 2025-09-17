package com.alebarre.cadastro_clientes.DTO;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record ClienteRequestDTO(
        @NotBlank @Size(min=3,max=120) String nome,
        @NotBlank @Email String email,
        @Pattern(regexp="\\d{10,11}", message="Telefone deve ter 10 ou 11 dígitos")
        String telefone,
        @Pattern(regexp="\\d{11}", message="CPF deve ter 11 dígitos")
        String cpf,
        LocalDate dataNascimento,
        @Size(max=2, message="No máximo 2 endereços")
        @NotNull List<@Valid EnderecoDTO> enderecos,
        @NotNull @Size(max = 5, message = "Máximo de 5 modalidades por cliente.")
        List<Long> modalidadeIds
) {}
