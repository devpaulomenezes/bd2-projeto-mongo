package br.edu.ifpb.es.bd2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "avaliacoes")
@CompoundIndexes({
        @CompoundIndex(name = "cliente_produto_unico", def = "{'clienteId': 1, 'produtoId': 1}", unique = true),
        @CompoundIndex(name = "produto_data", def = "{'produtoId': 1, 'criadoEm': -1}")
})
public class Avaliacao {

    @Id
    private String id;

    private String produtoId;

    private String clienteId;

    private String clienteNome;

    private Integer nota;

    private String comentario;

    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;
}
