package br.edu.ifpb.es.bd2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Nova quantidade de um item que já está no carrinho")
public record QuantidadeItemRequestDTO(
        @Schema(description = "Nova quantidade total do item (substitui a atual)", example = "3")
        @NotNull(message = "A quantidade é obrigatória")
        @Min(value = 1, message = "A quantidade deve ser no mínimo 1")
        Integer quantidade
) {}
