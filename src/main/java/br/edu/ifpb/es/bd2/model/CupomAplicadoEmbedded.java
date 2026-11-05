package br.edu.ifpb.es.bd2.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CupomAplicadoEmbedded {

    private String codigo;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal percentualDesconto;
}
