package com.alebarre.cadastro_clientes.controller;

import com.alebarre.cadastro_clientes.DTO.AppUserRequestDTO;
import com.alebarre.cadastro_clientes.DTO.AppUserResponseDTO;
import com.alebarre.cadastro_clientes.DTO.AppUserSummaryDTO;
import com.alebarre.cadastro_clientes.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AppUserController {

    private final AppUserService service;

    public AppUserController(AppUserService service) {
        this.service = service;
    }

    @GetMapping
    public List<AppUserSummaryDTO> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public AppUserResponseDTO find(@PathVariable Long id) {
        return service.find(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppUserResponseDTO create(@Valid @RequestBody AppUserRequestDTO req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public AppUserResponseDTO update(@PathVariable Long id, @Valid @RequestBody AppUserRequestDTO req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

