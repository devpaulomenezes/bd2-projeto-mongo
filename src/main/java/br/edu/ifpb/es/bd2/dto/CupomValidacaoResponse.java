package br.edu.ifpb.es.bd2.dto;

import java.math.BigDecimal;

public record CupomValidacaoResponse(
        boolean valido,
        String mensagem,
        BigDecimal percentualDesconto
) {}