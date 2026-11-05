package br.edu.ifpb.es.bd2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "pedidos")
@CompoundIndex(name = "cliente_data", def = "{'clienteId': 1, 'criadoEm': -1}")
public class Pedido {

    @Id
    private String id;

    private String clienteId;

    private ClienteSnapshotEmbedded cliente;

    private List<ItemPedidoEmbedded> itens;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal subtotal;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal desconto;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal valorTotal;

    private CupomAplicadoEmbedded cupom;

    @Setter
    @Indexed
    private StatusPedido status;

    @Setter
    private PagamentoEmbedded pagamento;

    @Setter
    private EntregaEmbedded entrega;

    @Setter
    private DevolucaoEmbedded devolucao;

    private LocalDateTime criadoEm;

    @Setter
    private LocalDateTime atualizadoEm;

    @Version
    private Long versao;
}
