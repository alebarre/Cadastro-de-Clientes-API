// src/main/java/com/alebarre/cadastro_clientes/DTO/AppUserSummaryDTO.java
package com.alebarre.cadastro_clientes.DTO;

public record AppUserSummaryDTO(
        Long id,
        String username,
        String nome,
        String email,
        String telefone,
        boolean enabled
) {}
