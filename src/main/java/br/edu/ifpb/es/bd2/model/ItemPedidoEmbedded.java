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
public class ItemPedidoEmbedded {

    private String produtoId;

    private String nome;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal precoUnitario;

    private Integer quantidade;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal subtotal;

    public static ItemPedidoEmbedded de(Produto produto, int quantidade) {
        BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(quantidade));
        return new ItemPedidoEmbedded(produto.getId(), produto.getNome(), produto.getPreco(), quantidade, subtotal);
    }
}
