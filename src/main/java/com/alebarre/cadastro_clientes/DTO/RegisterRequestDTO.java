package com.alebarre.cadastro_clientes.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank @Size(min=3, max=120) String nome,
        @NotBlank @Email String email,
        @NotBlank String password
) {}
