package br.edu.ifpb.es.bd2.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "cupons")
@Getter
@Setter
@NoArgsConstructor
public class Cupom {

    @Id
    private String id;

    @Indexed(unique = true)
    private String codigo;

    private BigDecimal percentualDesconto;

    private LocalDateTime dataExpiracao;

    private StatusCupom status;
}