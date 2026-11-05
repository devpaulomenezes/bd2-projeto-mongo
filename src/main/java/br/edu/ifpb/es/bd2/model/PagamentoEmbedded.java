package br.edu.ifpb.es.bd2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoEmbedded {

    private FormaPagamento forma;

    private StatusPagamento status;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal valor;

    private LocalDateTime atualizadoEm;
}
