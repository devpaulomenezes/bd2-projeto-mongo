package br.edu.ifpb.es.bd2.dto;

import br.edu.ifpb.es.bd2.model.Cupom;
import br.edu.ifpb.es.bd2.model.StatusCupom;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CupomResponse(
        String id,
        String codigo,
        BigDecimal percentualDesconto,
        LocalDateTime dataExpiracao,
        StatusCupom status
) {
    public static CupomResponse from(Cupom c) {
        return new CupomResponse(c.getId(), c.getCodigo(), c.getPercentualDesconto(),
                c.getDataExpiracao(), c.getStatus());
    }
}