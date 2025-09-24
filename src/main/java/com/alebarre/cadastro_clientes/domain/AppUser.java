package com.alebarre.cadastro_clientes.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "app_user")
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 120)
    @NotBlank @Size(min = 3, max = 120)
    private String username;

    @NotBlank
    private String password; // BCrypt

    @Column(nullable = false, length = 120)
    @NotBlank @Size(min = 3, max = 120)
    private String nome;

    @Column(unique = true, nullable = false, length = 180)
    @NotBlank @Email
    private String email;

    @Column(length = 20)
    @Pattern(regexp = "^$|^\\d{10,11}$",
            message = "Telefone deve ter 10 ou 11 dígitos (somente números)")
    private String telefone;

    private String roles = "ROLE_USER, ROLE_ADMIN";
    private boolean enabled;
}
