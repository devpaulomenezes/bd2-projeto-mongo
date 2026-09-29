package br.edu.ifpb.es.bd2.dto;

import br.edu.ifpb.es.bd2.model.EnderecoEmbedded;
import br.edu.ifpb.es.bd2.model.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UsuarioResponseDTO {
    private String id;
    private String nome;
    private String email;
    private Role role;
    private List<EnderecoEmbedded> enderecos;
    private LocalDateTime criadoEm;
    private Boolean ativo;
}
