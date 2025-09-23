package com.alebarre.cadastro_clientes.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppUserDTO {
    private Long id;
    private String username;
    private String password; // BCrypt
    private String nome;
    private String email;
    private String telefone;
    private String roles;
    private boolean enabled;
}
