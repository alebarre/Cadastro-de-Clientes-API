package com.alebarre.cadastro_clientes.controller;

import com.alebarre.cadastro_clientes.DTO.ClienteSummaryDTO;
import com.alebarre.cadastro_clientes.service.RelatorioClienteService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioClienteController {
    private final RelatorioClienteService service;

    public RelatorioClienteController(RelatorioClienteService service) { this.service = service; }

    @GetMapping("/clientes")
    public List<ClienteSummaryDTO> relatorioClientes(
            @RequestParam(required = false) Boolean ativos,
            @RequestParam(required = false) Boolean inativos,
            @RequestParam(required = false) Integer idadeMin,
            @RequestParam(required = false) Integer idadeMax,
            @RequestParam(required = false) List<Long> modalidades // ?modalidades=1,2,3
    ) {
        return service.buscar(ativos, inativos, idadeMin, idadeMax, modalidades);
    }
}

