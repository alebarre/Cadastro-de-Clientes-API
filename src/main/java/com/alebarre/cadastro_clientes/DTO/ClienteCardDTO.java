package com.alebarre.cadastro_clientes.DTO;

import com.alebarre.cadastro_clientes.domain.Cliente;
import com.alebarre.cadastro_clientes.domain.Endereco;
import com.alebarre.cadastro_clientes.domain.Modalidade;

import java.time.LocalDate;
import java.util.List;

public record ClienteCardDTO(
        Long id,
        String nome,
        String email,
        String telefone,
        List<EnderecoDTO> enderecos,
        List<ModalidadeDTO> modalidades,
        LocalDate dataNascimento
) {
    public static ClienteCardDTO fromEntity(Cliente c) {
        var ends = c.getEnderecos() == null ? List.<EnderecoDTO>of()
                : c.getEnderecos().stream().map(EnderecoDTO::fromEntity).toList();
        var mods = c.getModalidades() == null ? List.<ModalidadeDTO>of()
                : c.getModalidades().stream().map(ModalidadeDTO::fromEntity).toList();
        return new ClienteCardDTO(
                c.getId(), c.getNome(), c.getEmail(), c.getTelefone(), ends, mods, c.getDataNascimento()
        );
    }
}

