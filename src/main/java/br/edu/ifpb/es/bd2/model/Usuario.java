package br.edu.ifpb.es.bd2.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "usuarios")
public class Usuario {
    
    @Id
    private String id;
    
    private String nome;
    
    @Indexed(unique = true)
    private String email;
    
    private Role role;
    
    private List<EnderecoEmbedded> enderecos;
    
    private LocalDateTime criadoEm = LocalDateTime.now();
}
