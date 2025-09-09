package com.alebarre.cadastro_clientes.DTO;

public record RefreshResponseDTO(String accessToken, long expiresIn, String refreshToken) {}
