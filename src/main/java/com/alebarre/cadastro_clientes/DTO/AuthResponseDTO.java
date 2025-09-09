package com.alebarre.cadastro_clientes.DTO;

import com.alebarre.cadastro_clientes.domain.AppUser;

public record AuthResponseDTO(String refreshToken, String accessToken, long expiresIn, String username) {
}
