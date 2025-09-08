package com.alebarre.cadastro_clientes.DTO;

import java.util.List;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String email,
        String telefone,
        String cpf,
        List<EnderecoDTO> enderecos
) {}
