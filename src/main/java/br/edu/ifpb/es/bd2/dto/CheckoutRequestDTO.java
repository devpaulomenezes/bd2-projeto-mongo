package br.edu.ifpb.es.bd2.dto;

import br.edu.ifpb.es.bd2.model.FormaPagamento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para finalizar a compra do carrinho de um cliente")
public record CheckoutRequestDTO(
        @Schema(description = "Id do cliente dono do carrinho")
        @NotBlank(message = "O cliente é obrigatório")
        String clienteId,

        @Schema(description = "Código do cupom de desconto, opcional", example = "BEMVINDO10")
        String cupomCodigo,

        @Schema(description = "Forma de pagamento", example = "PIX")
        @NotNull(message = "A forma de pagamento é obrigatória")
        FormaPagamento formaPagamento,

        @Schema(description = "Endereço de entrega; se ausente, é usado o primeiro endereço cadastrado do cliente")
        @Valid
        EnderecoEntregaDTO enderecoEntrega
) {}
