package br.edu.ifpb.es.bd2.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "DTO padrão para respostas de mensagens e status de operações")
public record MensagemResponseDTO(
        @Schema(description = "Mensagem descritiva da operação", example = "Operação realizada com sucesso")
        String mensagem,

        @Schema(description = "Data e hora em que a resposta foi gerada", example = "2026-08-29T14:30:00")
        LocalDateTime timestamp
) {
    public MensagemResponseDTO(String mensagem) {
        this(mensagem, LocalDateTime.now());
    }

    public static MensagemResponseDTO of(String mensagem) {
        return new MensagemResponseDTO(mensagem);
    }
}
