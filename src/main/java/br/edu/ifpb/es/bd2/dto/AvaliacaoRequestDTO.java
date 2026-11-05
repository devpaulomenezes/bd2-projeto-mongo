package br.edu.ifpb.es.bd2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para registrar ou alterar a avaliação de um produto")
public record AvaliacaoRequestDTO(
        @Schema(description = "Id do cliente que avalia o produto")
        @NotBlank(message = "O cliente é obrigatório")
        String clienteId,

        @Schema(description = "Nota de 1 a 5", example = "5")
        @NotNull(message = "A nota é obrigatória")
        @Min(value = 1, message = "A nota deve ser no mínimo 1")
        @Max(value = 5, message = "A nota deve ser no máximo 5")
        Integer nota,

        @Schema(description = "Comentário sobre o produto", example = "Chegou rápido e funciona muito bem")
        @Size(max = 1000, message = "O comentário deve ter no máximo 1000 caracteres")
        String comentario
) {}
