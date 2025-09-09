package com.alebarre.cadastro_clientes.DTO;

import com.alebarre.cadastro_clientes.domain.AppUser;

public record AuthResponseDTO(String token, long expiresIn, String username) {
}
