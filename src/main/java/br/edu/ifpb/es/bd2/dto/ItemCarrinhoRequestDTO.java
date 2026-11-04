package br.edu.ifpb.es.bd2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para adicionar um produto ao carrinho")
public record ItemCarrinhoRequestDTO(
        @Schema(description = "Id do produto a ser adicionado")
        @NotBlank(message = "O produto é obrigatório")
        String produtoId,

        @Schema(description = "Quantidade a adicionar (somada à existente, se o produto já estiver no carrinho)", example = "2")
        @NotNull(message = "A quantidade é obrigatória")
        @Min(value = 1, message = "A quantidade deve ser no mínimo 1")
        Integer quantidade
) {}
