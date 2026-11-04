package br.edu.ifpb.es.bd2.dto;

import br.edu.ifpb.es.bd2.model.ItemCarrinhoEmbedded;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Item do carrinho com o preço registrado no momento da adição")
public record ItemCarrinhoResponseDTO(
        String produtoId,
        String nome,
        BigDecimal precoUnitario,
        Integer quantidade,
        BigDecimal subtotal
) {
    public static ItemCarrinhoResponseDTO from(ItemCarrinhoEmbedded i) {
        return new ItemCarrinhoResponseDTO(i.getProdutoId(), i.getNome(), i.getPrecoUnitario(),
                i.getQuantidade(), i.getSubtotal());
    }
}
