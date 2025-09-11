package com.alebarre.cadastro_clientes.service;

import com.alebarre.cadastro_clientes.DTO.ModalidadeDTO;
import com.alebarre.cadastro_clientes.DTO.ModalidadeUpsertDTO;
import com.alebarre.cadastro_clientes.domain.Modalidade;
import com.alebarre.cadastro_clientes.repository.ModalidadeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service @RequiredArgsConstructor
public class ModalidadeService {
    private final ModalidadeRepository repo;

    public List<ModalidadeDTO> listar() {
        return repo.findAll().stream().map(this::toDTO).toList();
    }
    public ModalidadeDTO obter(Long id) {
        return toDTO(repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Modalidade não encontrada")));
    }
    public ModalidadeDTO criar(ModalidadeUpsertDTO dto) {
        var m = new Modalidade();
        aplicar(m, dto);
        return toDTO(repo.save(m));
    }
    public ModalidadeDTO atualizar(Long id, ModalidadeUpsertDTO dto) {
        var m = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Modalidade não encontrada"));
        aplicar(m, dto);
        return toDTO(repo.save(m));
    }
    public void excluir(Long id) {
        if (!repo.existsById(id)) throw new EntityNotFoundException("Modalidade não encontrada");
        repo.deleteById(id);
    }

    private void aplicar(Modalidade m, ModalidadeUpsertDTO dto) {
        m.setNome(dto.nome());
        m.setDescricao(dto.descricao());
        m.setValor(dto.valor());
    }
    private ModalidadeDTO toDTO(Modalidade m) {
        return new ModalidadeDTO(m.getId(), m.getNome(), m.getDescricao(), m.getValor());
    }
}

