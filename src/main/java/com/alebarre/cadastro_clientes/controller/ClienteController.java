package com.alebarre.cadastro_clientes.controller;

import com.alebarre.cadastro_clientes.DTO.ClienteRequestDTO;
import com.alebarre.cadastro_clientes.DTO.ClienteResponseDTO;
import com.alebarre.cadastro_clientes.DTO.ClienteSummaryDTO;
import com.alebarre.cadastro_clientes.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService service;
    public ClienteController(ClienteService service) { this.service = service; }

    @GetMapping
    public List<ClienteSummaryDTO> list() { return service.list(); }

    @GetMapping("/{id}")
    public ClienteResponseDTO get(@PathVariable Long id) { return service.find(id); }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> create(@RequestBody @Valid ClienteRequestDTO req) {
        var saved = service.create(req);
        return ResponseEntity.created(URI.create("/api/clientes/" + saved.id())).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ClienteResponseDTO update(@PathVariable Long id, @RequestBody @Valid ClienteRequestDTO req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
