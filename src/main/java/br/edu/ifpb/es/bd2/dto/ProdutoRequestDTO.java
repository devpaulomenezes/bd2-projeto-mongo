package br.edu.ifpb.es.bd2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Dados para criação ou atualização de um produto")
public record ProdutoRequestDTO(
        @Schema(description = "Nome do produto", example = "Notebook Pro 14")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        String nome,

        @Schema(description = "Descrição do produto", example = "Notebook com 16 GB de RAM e SSD de 512 GB")
        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        String descricao,

        @Schema(description = "Preço unitário", example = "4599.90")
        @NotNull(message = "O preço é obrigatório")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
        BigDecimal preco,

        @Schema(description = "Quantidade em estoque", example = "10")
        @NotNull(message = "O estoque é obrigatório")
        @Min(value = 0, message = "O estoque não pode ser negativo")
        Integer estoque,

        @Schema(description = "Id da categoria do produto")
        @NotBlank(message = "A categoria é obrigatória")
        String categoriaId,

        @Schema(description = "Id do usuário vendedor do produto")
        @NotBlank(message = "O vendedor é obrigatório")
        String vendedorId
) {}
