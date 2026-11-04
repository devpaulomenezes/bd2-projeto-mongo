package br.edu.ifpb.es.bd2.dto;

import br.edu.ifpb.es.bd2.model.Produto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Produto do catálogo com dados desnormalizados de categoria e vendedor")
public record ProdutoResponseDTO(
        String id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer estoque,
        String categoriaId,
        String categoriaNome,
        String vendedorId,
        String vendedorNome,
        Double mediaAvaliacoes,
        Integer totalAvaliacoes
) {
    public static ProdutoResponseDTO from(Produto p) {
        return new ProdutoResponseDTO(p.getId(), p.getNome(), p.getDescricao(), p.getPreco(), p.getEstoque(),
                p.getCategoriaId(), p.getCategoriaNome(), p.getVendedorId(), p.getVendedorNome(),
                p.getMediaAvaliacoes(), p.getTotalAvaliacoes());
    }
}
