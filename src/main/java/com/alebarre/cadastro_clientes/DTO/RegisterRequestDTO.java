package com.alebarre.cadastro_clientes.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(@NotBlank @Email String email,
                          @NotBlank @Size(min = 6, max = 100) String password) {
}
