package com.alebarre.cadastro_clientes.DTO;

import com.alebarre.cadastro_clientes.domain.Modalidade;

import java.math.BigDecimal;

public record ModalidadeDTO(Long id, String nome, String descricao, BigDecimal valor) {
    public static ModalidadeDTO fromEntity(Modalidade m) {
        return new ModalidadeDTO(m.getId(), m.getNome(), m.getDescricao(), m.getValor());
    }
}
