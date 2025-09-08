package com.alebarre.cadastro_clientes.service;

import com.alebarre.cadastro_clientes.DTO.ClienteRequestDTO;
import com.alebarre.cadastro_clientes.DTO.ClienteResponseDTO;
import com.alebarre.cadastro_clientes.DTO.ClienteSummaryDTO;
import com.alebarre.cadastro_clientes.DTO.EnderecoDTO;
import com.alebarre.cadastro_clientes.domain.Cliente;
import com.alebarre.cadastro_clientes.domain.Endereco;
import com.alebarre.cadastro_clientes.repository.ClienteRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) { this.clienteRepository = clienteRepository; }

    public List<ClienteSummaryDTO> list() {
        return clienteRepository.findAll().stream().map(c -> {
            // extrai até 2 cidades distintas
            List<String> unicas = c.getEnderecos().stream()
                    .map(Endereco::getCidade)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .limit(2)
                    .toList();
            return new ClienteSummaryDTO(c.getId(), c.getNome(), c.getEmail(), c.getTelefone(), unicas);
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

    @Transactional
    public void delete(Long id) {
        if (!clienteRepository.existsById(id)) throw new EntityNotFoundException("Cliente não encontrado");
        clienteRepository.deleteById(id);
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
                new EnderecoDTO(e.getId(), e.getLogradouro(), e.getNumero(), e.getComplemento(),
                        e.getBairro(), e.getCidade(), e.getUf(), e.getCep())
        ).toList();

        return new ClienteResponseDTO(c.getId(), c.getNome(), c.getEmail(), c.getTelefone(), c.getCpf(), ends);
    }
}
