package com.alebarre.cadastro_clientes.DTO;

import java.time.LocalDate;
import java.util.List;

public record ClienteResponseDTO(
        Long id,
        String nome,
        Boolean enabled,
        String email,
        String telefone,
        String cpf,
        LocalDate dataNascimento,
        List<EnderecoDTO> enderecos,
        List<ModalidadeDTO> modalidades
) {}
