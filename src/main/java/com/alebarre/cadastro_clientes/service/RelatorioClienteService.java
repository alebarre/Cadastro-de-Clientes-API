package com.alebarre.cadastro_clientes.service;

import com.alebarre.cadastro_clientes.DTO.ClienteSummaryDTO;
import com.alebarre.cadastro_clientes.domain.Cliente;
import com.alebarre.cadastro_clientes.repository.ClienteRepository;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelatorioClienteService {
    private final ClienteRepository repo;

    public RelatorioClienteService(ClienteRepository repo) { this.repo = repo; }

    public List<ClienteSummaryDTO> buscar(
            Boolean ativos, Boolean inativos,
            Integer idadeMin, Integer idadeMax,
            List<Long> modalidadeIds
    ) {
        // Avalia tri-estado com segurança
        boolean onlyAtivos   = Boolean.TRUE.equals(ativos)   && !Boolean.TRUE.equals(inativos);
        boolean onlyInativos = Boolean.TRUE.equals(inativos) && !Boolean.TRUE.equals(ativos);

        // Vamos acumular partes e combinar no fim (evita .and sobre null)
        List<Specification<Cliente>> parts = new ArrayList<>();

        // Status
        if (onlyAtivos) {
            parts.add((root, q, cb) -> cb.isTrue(root.get("enabled")));
        } else if (onlyInativos) {
            parts.add((root, q, cb) -> cb.isFalse(root.get("enabled")));
        }
        // (se ambos true ou ambos null/false → não filtra por status)

        // Faixa etária (opcional)
        if (idadeMin != null || idadeMax != null) {
            parts.add(faixaEtaria(idadeMin, idadeMax));
        }

        // Modalidades (opcional)
        if (modalidadeIds != null && !modalidadeIds.isEmpty()) {
            parts.add(comAlgumaModalidade(modalidadeIds));
        }

        // Combina todas as partes com AND; se vazio, passa null (sem filtro)
        Specification<Cliente> spec = null;
        for (Specification<Cliente> s : parts) {
            spec = (spec == null) ? s : spec.and(s);
        }

        return repo.findAll(spec).stream()
                .map(ClienteSummaryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    private Specification<Cliente> comAlgumaModalidade(List<Long> ids) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Object, Object> join = root.join("modalidades", JoinType.INNER);
            CriteriaBuilder.In<Object> in = cb.in(join.get("id"));
            ids.forEach(in::value);
            return in;
        };
    }

    private Specification<Cliente> faixaEtaria(Integer idadeMin, Integer idadeMax) {
        return (root, query, cb) -> {
            Path<LocalDate> dataNascimento = root.get("dataNascimento");
            LocalDate hoje = LocalDate.now();

            List<jakarta.persistence.criteria.Predicate> preds = new ArrayList<>();

            // idade >= min  => nascimento <= hoje.minusYears(min)
            if (idadeMin != null) {
                LocalDate limiteMaxNascimento = hoje.minusYears(idadeMin);
                preds.add(cb.lessThanOrEqualTo(dataNascimento, limiteMaxNascimento));
            }
            // idade <= max  => nascimento >= hoje.minusYears(max + 1).plusDays(1)
            if (idadeMax != null) {
                LocalDate limiteMinNascimento = hoje.minusYears(idadeMax + 1).plusDays(1);
                preds.add(cb.greaterThanOrEqualTo(dataNascimento, limiteMinNascimento));
            }

            return preds.isEmpty()
                    ? cb.conjunction()
                    : cb.and(preds.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
