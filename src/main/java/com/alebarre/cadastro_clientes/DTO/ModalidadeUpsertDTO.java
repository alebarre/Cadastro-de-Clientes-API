package com.alebarre.cadastro_clientes.DTO;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ModalidadeUpsertDTO(
        @NotBlank @Size(max=120) String nome,
        @NotBlank @Size(max=255) String descricao,
        @NotNull @DecimalMin("0.00") BigDecimal valor
) {}

