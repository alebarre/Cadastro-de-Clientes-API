package com.alebarre.cadastro_clientes.controller;

import jakarta.validation.constraints.NotBlank;

record AuthRequest(@NotBlank String username, @NotBlank String password) {
}
