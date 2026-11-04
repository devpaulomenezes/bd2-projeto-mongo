package br.edu.ifpb.es.bd2.dto;

import br.edu.ifpb.es.bd2.model.EnderecoEmbedded;
import br.edu.ifpb.es.bd2.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UsuarioRequestDTO {
    @NotBlank
    private String nome;
    
    @NotBlank
    @Email
    private String email;
    
    @NotNull
    private Role role;
    
    private List<EnderecoEmbedded> enderecos;
}
