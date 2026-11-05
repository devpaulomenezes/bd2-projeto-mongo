package br.edu.ifpb.es.bd2.dto;

import br.edu.ifpb.es.bd2.model.Avaliacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Avaliação de um produto feita por um cliente")
public record AvaliacaoResponseDTO(
        String id,
        String produtoId,
        String clienteId,
        String clienteNome,
        Integer nota,
        String comentario,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static AvaliacaoResponseDTO from(Avaliacao a) {
        return new AvaliacaoResponseDTO(a.getId(), a.getProdutoId(), a.getClienteId(), a.getClienteNome(),
                a.getNota(), a.getComentario(), a.getCriadoEm(), a.getAtualizadoEm());
    }
}
