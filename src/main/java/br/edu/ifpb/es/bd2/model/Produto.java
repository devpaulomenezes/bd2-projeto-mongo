package br.edu.ifpb.es.bd2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "produtos")
public class Produto {

    @Id
    private String id;

    @Indexed
    private String nome;

    private String descricao;

    // Decimal128 em vez do padrao String do Spring Data, para o filtro de faixa de preco comparar numericamente
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal preco;

    private Integer estoque;

    @Indexed
    private String categoriaId;

    private String categoriaNome;

    @Indexed
    private String vendedorId;

    private String vendedorNome;

    @Builder.Default
    private Double mediaAvaliacoes = 0.0;

    @Builder.Default
    private Integer totalAvaliacoes = 0;
}
