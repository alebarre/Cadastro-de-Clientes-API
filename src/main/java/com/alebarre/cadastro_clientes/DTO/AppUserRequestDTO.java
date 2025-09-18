// src/main/java/com/alebarre/cadastro_clientes/DTO/AppUserRequestDTO.java
package com.alebarre.cadastro_clientes.DTO;

import jakarta.validation.constraints.*;
import java.util.List;

public record AppUserRequestDTO(
        @NotBlank @Size(min=3, max=120) String username,
        String password,                       // obrigatório no create, opcional no update
        @NotBlank @Size(min=3, max=120) String nome,
        @NotBlank @Email String email,
        @Pattern(regexp="^$|^\\d{10,11}$") String telefone,
        List<String> roles,
        Boolean enabled
) {}
