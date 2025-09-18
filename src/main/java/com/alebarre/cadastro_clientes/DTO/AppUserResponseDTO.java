// src/main/java/com/alebarre/cadastro_clientes/DTO/AppUserResponseDTO.java
package com.alebarre.cadastro_clientes.DTO;

import java.util.List;

public record AppUserResponseDTO(
        Long id,
        String username,
        String nome,
        String email,
        String telefone,
        List<String> roles,
        boolean enabled
) {}
