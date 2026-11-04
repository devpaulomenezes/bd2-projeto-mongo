package br.edu.ifpb.es.bd2.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resumo de uma execução da rotina de limpeza de carrinhos")
public record LimpezaCarrinhoResponseDTO(
        @Schema(description = "Carrinhos removidos por inatividade", example = "3")
        long carrinhosInativosRemovidos,

        @Schema(description = "Itens removidos porque o produto não existe mais no catálogo", example = "2")
        int itensIndisponiveisRemovidos,

        @Schema(description = "Carrinhos removidos por ficarem sem itens", example = "1")
        long carrinhosVaziosRemovidos,

        @Schema(description = "Data e hora da execução")
        LocalDateTime executadoEm
) {}
