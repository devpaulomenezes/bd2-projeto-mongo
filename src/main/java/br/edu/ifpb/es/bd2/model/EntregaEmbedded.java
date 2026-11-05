package br.edu.ifpb.es.bd2.model;

import lombok.AccessLevel;
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
public class EntregaEmbedded {

    @Setter(AccessLevel.NONE)
    private EnderecoEmbedded endereco;

    private StatusEntrega status;

    private String codigoRastreio;

    private LocalDateTime enviadoEm;

    private LocalDateTime entregueEm;
}
