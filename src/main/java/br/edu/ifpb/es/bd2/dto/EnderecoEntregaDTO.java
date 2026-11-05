package br.edu.ifpb.es.bd2.dto;

import br.edu.ifpb.es.bd2.model.EnderecoEmbedded;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Endereço de entrega do pedido")
public record EnderecoEntregaDTO(
        @Schema(example = "Rua das Flores")
        @NotBlank(message = "O logradouro é obrigatório")
        String logradouro,

        @Schema(example = "120")
        @NotBlank(message = "O número é obrigatório")
        String numero,

        @Schema(example = "Centro")
        String bairro,

        @Schema(example = "João Pessoa")
        @NotBlank(message = "A cidade é obrigatória")
        String cidade,

        @Schema(example = "PB")
        @NotBlank(message = "O estado é obrigatório")
        String estado,

        @Schema(example = "58010-000")
        @NotBlank(message = "O CEP é obrigatório")
        String cep
) {
    public EnderecoEmbedded toEmbedded() {
        EnderecoEmbedded endereco = new EnderecoEmbedded();
        endereco.setLogradouro(logradouro);
        endereco.setNumero(numero);
        endereco.setBairro(bairro);
        endereco.setCidade(cidade);
        endereco.setEstado(estado);
        endereco.setCep(cep);
        return endereco;
    }

    public static EnderecoEntregaDTO from(EnderecoEmbedded e) {
        return new EnderecoEntregaDTO(e.getLogradouro(), e.getNumero(), e.getBairro(),
                e.getCidade(), e.getEstado(), e.getCep());
    }
}
