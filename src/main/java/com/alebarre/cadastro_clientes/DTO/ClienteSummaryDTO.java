package com.alebarre.cadastro_clientes.DTO;


import java.util.List;

public record ClienteSummaryDTO(
        Long id,
        String nome,
        String email,
        String telefone,
        List<String> cidades // até 2 cidades para lista
) {}
