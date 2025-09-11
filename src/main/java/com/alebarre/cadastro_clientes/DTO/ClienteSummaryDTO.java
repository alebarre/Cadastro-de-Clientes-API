package com.alebarre.cadastro_clientes.DTO;


import com.alebarre.cadastro_clientes.domain.Cliente;

import java.util.List;

public record ClienteSummaryDTO(
        Long id,
        String nome,
        String email,
        String telefone,
        String enderecosResumo,      // cidades separadas por " | "
        int quantidadeModalidades
) {
    public static ClienteSummaryDTO fromEntity(Cliente c) {
        // monta o resumo de cidades (máx. 2, separados por " | ")
        List<String> cidades = c.getEnderecos() == null ? List.of()
                : c.getEnderecos().stream()
                .map(e -> e.getCidade())
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .limit(2)
                .toList();

        String resumo = String.join(" | ", cidades);

        int qModal = (c.getModalidades() == null) ? 0 : c.getModalidades().size();

        return new ClienteSummaryDTO(
                c.getId(),
                c.getNome(),
                c.getEmail(),
                c.getTelefone(),
                resumo,
                qModal
        );
    }
}