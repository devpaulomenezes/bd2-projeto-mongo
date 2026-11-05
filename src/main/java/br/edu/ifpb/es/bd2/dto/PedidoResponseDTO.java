package br.edu.ifpb.es.bd2.dto;

import br.edu.ifpb.es.bd2.model.DevolucaoEmbedded;
import br.edu.ifpb.es.bd2.model.EntregaEmbedded;
import br.edu.ifpb.es.bd2.model.FormaPagamento;
import br.edu.ifpb.es.bd2.model.ItemPedidoEmbedded;
import br.edu.ifpb.es.bd2.model.PagamentoEmbedded;
import br.edu.ifpb.es.bd2.model.Pedido;
import br.edu.ifpb.es.bd2.model.StatusDevolucao;
import br.edu.ifpb.es.bd2.model.StatusEntrega;
import br.edu.ifpb.es.bd2.model.StatusPagamento;
import br.edu.ifpb.es.bd2.model.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Pedido com os dados de cliente, itens, preços e endereço congelados no checkout")
public record PedidoResponseDTO(
        String id,
        String clienteId,
        ClientePedido cliente,
        List<ItemPedido> itens,
        BigDecimal subtotal,
        BigDecimal desconto,
        BigDecimal valorTotal,
        CupomPedido cupom,
        StatusPedido status,
        PagamentoPedido pagamento,
        EntregaPedido entrega,
        DevolucaoPedido devolucao,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static PedidoResponseDTO from(Pedido p) {
        List<ItemPedido> itens = p.getItens() == null ? List.of() : p.getItens().stream().map(ItemPedido::from).toList();
        return new PedidoResponseDTO(
                p.getId(),
                p.getClienteId(),
                p.getCliente() == null ? null : new ClientePedido(p.getCliente().getNome(), p.getCliente().getEmail()),
                itens,
                p.getSubtotal(),
                p.getDesconto(),
                p.getValorTotal(),
                p.getCupom() == null ? null : new CupomPedido(p.getCupom().getCodigo(), p.getCupom().getPercentualDesconto()),
                p.getStatus(),
                PagamentoPedido.from(p.getPagamento()),
                EntregaPedido.from(p.getEntrega()),
                DevolucaoPedido.from(p.getDevolucao()),
                p.getCriadoEm(),
                p.getAtualizadoEm());
    }

    @Schema(description = "Dados do cliente no momento da compra")
    public record ClientePedido(String nome, String email) {}

    @Schema(description = "Cupom aplicado no checkout")
    public record CupomPedido(String codigo, BigDecimal percentualDesconto) {}

    @Schema(description = "Item com o preço do catálogo no momento da compra")
    public record ItemPedido(String produtoId, String nome, BigDecimal precoUnitario, Integer quantidade,
                             BigDecimal subtotal) {
        static ItemPedido from(ItemPedidoEmbedded i) {
            return new ItemPedido(i.getProdutoId(), i.getNome(), i.getPrecoUnitario(), i.getQuantidade(), i.getSubtotal());
        }
    }

    @Schema(description = "Situação do pagamento")
    public record PagamentoPedido(FormaPagamento forma, StatusPagamento status, BigDecimal valor,
                                  LocalDateTime atualizadoEm) {
        static PagamentoPedido from(PagamentoEmbedded p) {
            return p == null ? null : new PagamentoPedido(p.getForma(), p.getStatus(), p.getValor(), p.getAtualizadoEm());
        }
    }

    @Schema(description = "Situação da entrega")
    public record EntregaPedido(EnderecoEntregaDTO endereco, StatusEntrega status, String codigoRastreio,
                                LocalDateTime enviadoEm, LocalDateTime entregueEm) {
        static EntregaPedido from(EntregaEmbedded e) {
            if (e == null) {
                return null;
            }
            EnderecoEntregaDTO endereco = e.getEndereco() == null ? null : EnderecoEntregaDTO.from(e.getEndereco());
            return new EntregaPedido(endereco, e.getStatus(), e.getCodigoRastreio(), e.getEnviadoEm(), e.getEntregueEm());
        }
    }

    @Schema(description = "Situação da devolução, quando solicitada")
    public record DevolucaoPedido(String motivo, StatusDevolucao status, LocalDateTime solicitadaEm,
                                  LocalDateTime atualizadaEm) {
        static DevolucaoPedido from(DevolucaoEmbedded d) {
            return d == null ? null : new DevolucaoPedido(d.getMotivo(), d.getStatus(), d.getSolicitadaEm(), d.getAtualizadaEm());
        }
    }
}
