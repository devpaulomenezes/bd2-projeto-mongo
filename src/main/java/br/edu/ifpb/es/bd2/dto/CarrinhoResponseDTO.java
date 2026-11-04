package br.edu.ifpb.es.bd2.dto;

import br.edu.ifpb.es.bd2.model.Carrinho;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Carrinho do cliente com itens embutidos e valor total calculado")
public record CarrinhoResponseDTO(
        String id,
        String clienteId,
        List<ItemCarrinhoResponseDTO> itens,
        BigDecimal valorTotal
) {
    public static CarrinhoResponseDTO from(Carrinho c) {
        List<ItemCarrinhoResponseDTO> itens = c.getItens().stream().map(ItemCarrinhoResponseDTO::from).toList();
        return new CarrinhoResponseDTO(c.getId(), c.getClienteId(), itens, c.getValorTotal());
    }
}
