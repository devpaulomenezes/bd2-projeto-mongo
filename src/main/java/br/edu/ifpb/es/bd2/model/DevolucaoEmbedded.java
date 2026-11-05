package br.edu.ifpb.es.bd2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevolucaoEmbedded {

    private String motivo;

    private StatusDevolucao status;

    private LocalDateTime solicitadaEm;

    private LocalDateTime atualizadaEm;
}
