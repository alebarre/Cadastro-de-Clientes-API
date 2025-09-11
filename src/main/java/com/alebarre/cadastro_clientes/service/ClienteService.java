package com.alebarre.cadastro_clientes.service;

import com.alebarre.cadastro_clientes.DTO.*;
import com.alebarre.cadastro_clientes.domain.Cliente;
import com.alebarre.cadastro_clientes.domain.Endereco;
import com.alebarre.cadastro_clientes.repository.ClienteRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) { this.clienteRepository = clienteRepository; }

    public List<ClienteSummaryDTO> list() {
        return clienteRepository.findAll().stream().map(c -> {
            // até 2 cidades distintas, concatenadas por " | "
            String enderecosResumo =
                    (c.getEnderecos() == null ? java.util.stream.Stream.<String>empty()
                            : c.getEnderecos().stream()
                            .map(e -> e.getCidade())
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .distinct()
                            .limit(2))
                            .collect(Collectors.joining(" | "));

            int quantidadeModalidades = (c.getModalidades() == null) ? 0 : c.getModalidades().size();

            return new ClienteSummaryDTO(
                    c.getId(),
                    c.getNome(),
                    c.getEmail(),
                    c.getTelefone(),
                    enderecosResumo,
                    quantidadeModalidades
            );
        }).toList();
    }

    public ClienteResponseDTO find(Long id) {
        Cliente c = clienteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        return toResponse(c);
    }

    @Transactional
    public ClienteResponseDTO create(ClienteRequestDTO req) {
        if (clienteRepository.existsByEmail(req.email())) throw new ValidationException("Email já cadastrado");
        Cliente c = fromRequest(new Cliente(), req);
        return toResponse(clienteRepository.save(c));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ClienteResponseDTO update(Long id, ClienteRequestDTO req) {
        Cliente c = clienteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        // se email mudou, garantir unicidade
        if (!c.getEmail().equals(req.email()) && clienteRepository.existsByEmail(req.email())) {
            throw new ValidationException("Email já cadastrado");
        }
        fromRequest(c, req);
        return toResponse(clienteRepository.save(c));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {
        if (!clienteRepository.existsById(id)) throw new EntityNotFoundException("Cliente não encontrado");
        clienteRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ClienteSummaryDTO> listarSummaries() {
        return clienteRepository.findAll().stream()
                .map(ClienteSummaryDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteCardDTO obterCard(Long id) {
        var c = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        return ClienteCardDTO.fromEntity(c);
    }

    // ===== mapeamento =====
    private Cliente fromRequest(Cliente c, ClienteRequestDTO req) {
        if (req.enderecos() == null || req.enderecos().isEmpty())
            throw new ValidationException("Informe ao menos 1 endereço");
        if (req.enderecos().size() > 2)
            throw new ValidationException("No máximo 2 endereços");

        c.setNome(req.nome());
        c.setEmail(req.email());
        c.setTelefone(req.telefone());
        c.setCpf(req.cpf());

        List<Endereco> ends = req.enderecos().stream().map(d -> {
            Endereco e = new Endereco();
            e.setLogradouro(d.logradouro());
            e.setNumero(d.numero());
            e.setComplemento(d.complemento());
            e.setBairro(d.bairro());
            e.setCidade(d.cidade());
            e.setUf(d.uf().toUpperCase());
            e.setCep(d.cep());
            return e;
        }).toList();

        c.setEnderecos(ends);
        return c;
    }

    private ClienteResponseDTO toResponse(Cliente c) {
        var ends = c.getEnderecos().stream().map(e ->
                new EnderecoDTO(e.getId(), e.getCidade(), e.getLogradouro(), e.getUf(), e.getBairro(), e.getNumero(), e.getComplemento(),
                        e.getCep())
        ).toList();

        var mods = c.getModalidades().stream().map(e ->
                new ModalidadeDTO(e.getId(), e.getNome(), e.getDescricao(), e.getValor())
        ).toList();

        return new ClienteResponseDTO(c.getId(), c.getNome(), c.getEmail(), c.getTelefone(), c.getCpf(), ends, mods);
    }
}
