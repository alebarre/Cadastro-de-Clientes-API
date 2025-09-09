package com.alebarre.cadastro_clientes.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotRequestDTO(@NotBlank @Email String email) {
}
