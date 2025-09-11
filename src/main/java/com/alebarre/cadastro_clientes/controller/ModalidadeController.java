package com.alebarre.cadastro_clientes.controller;

import com.alebarre.cadastro_clientes.DTO.ModalidadeDTO;
import com.alebarre.cadastro_clientes.DTO.ModalidadeUpsertDTO;
import com.alebarre.cadastro_clientes.service.ModalidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller com CRUD (substitui o mínimo acima)
@RestController
@RequestMapping("/api/modalidades")
@RequiredArgsConstructor
public class ModalidadeController {
    private final ModalidadeService svc;

    @GetMapping
    public List<ModalidadeDTO> listar() {
        return svc.listar();
    }

    @GetMapping("/{id}")
    public ModalidadeDTO obter(@PathVariable Long id) {
        return svc.obter(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ModalidadeDTO criar(@jakarta.validation.Valid @RequestBody ModalidadeUpsertDTO dto) {
        return svc.criar(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ModalidadeDTO atualizar(@PathVariable Long id, @jakarta.validation.Valid @RequestBody ModalidadeUpsertDTO dto) {
        return svc.atualizar(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        svc.excluir(id);
    }
}

