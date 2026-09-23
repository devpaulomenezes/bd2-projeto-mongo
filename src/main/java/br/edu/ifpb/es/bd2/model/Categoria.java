package br.edu.ifpb.es.bd2.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@Document(collection = "categorias")
public class Categoria {

    @Id
    private Long id;

    @Indexed(unique = true)
    private String nome;

    private String descricao;
}
