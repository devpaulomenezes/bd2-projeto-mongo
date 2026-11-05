package br.edu.ifpb.es.bd2.service;

import br.edu.ifpb.es.bd2.dto.CheckoutRequestDTO;
import br.edu.ifpb.es.bd2.dto.CupomValidacaoResponse;
import br.edu.ifpb.es.bd2.dto.EnderecoEntregaDTO;
import br.edu.ifpb.es.bd2.dto.PaginaResponseDTO;
import br.edu.ifpb.es.bd2.dto.PedidoResponseDTO;
import br.edu.ifpb.es.bd2.exception.CarrinhoVazioException;
import br.edu.ifpb.es.bd2.exception.ClienteInvalidoException;
import br.edu.ifpb.es.bd2.exception.CupomInvalidoException;
import br.edu.ifpb.es.bd2.exception.RecursoNaoEncontradoException;
import br.edu.ifpb.es.bd2.exception.RequisicaoInvalidaException;
import br.edu.ifpb.es.bd2.model.Carrinho;
import br.edu.ifpb.es.bd2.model.ClienteSnapshotEmbedded;
import br.edu.ifpb.es.bd2.model.CupomAplicadoEmbedded;
import br.edu.ifpb.es.bd2.model.EnderecoEmbedded;
import br.edu.ifpb.es.bd2.model.EntregaEmbedded;
import br.edu.ifpb.es.bd2.model.FormaPagamento;
import br.edu.ifpb.es.bd2.model.ItemCarrinhoEmbedded;
import br.edu.ifpb.es.bd2.model.ItemPedidoEmbedded;
import br.edu.ifpb.es.bd2.model.PagamentoEmbedded;
import br.edu.ifpb.es.bd2.model.Pedido;
import br.edu.ifpb.es.bd2.model.Produto;
import br.edu.ifpb.es.bd2.model.Role;
import br.edu.ifpb.es.bd2.model.StatusEntrega;
import br.edu.ifpb.es.bd2.model.StatusPagamento;
import br.edu.ifpb.es.bd2.model.StatusPedido;
import br.edu.ifpb.es.bd2.model.Usuario;
import br.edu.ifpb.es.bd2.repository.CarrinhoRepository;
import br.edu.ifpb.es.bd2.repository.PedidoRepository;
import br.edu.ifpb.es.bd2.repository.ProdutoRepository;
import br.edu.ifpb.es.bd2.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final PedidoRepository repository;
    private final CarrinhoRepository carrinhoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoService produtoService;
    private final CupomService cupomService;

    public PedidoResponseDTO checkout(CheckoutRequestDTO req) {
        Usuario cliente = validarCliente(req.clienteId());
        Carrinho carrinho = carrinhoRepository.findByClienteId(cliente.getId())
                .filter(c -> c.getItens() != null && !c.getItens().isEmpty())
                .orElseThrow(() -> new CarrinhoVazioException(cliente.getId()));
        CupomAplicadoEmbedded cupom = aplicarCupom(req.cupomCodigo());
        EnderecoEmbedded endereco = definirEndereco(req.enderecoEntrega(), cliente);

        List<ItemPedidoEmbedded> debitados = new ArrayList<>();
        Pedido salvo;
        try {
            for (ItemCarrinhoEmbedded item : carrinho.getItens()) {
                Produto produto = produtoRepository.findById(item.getProdutoId())
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", item.getProdutoId()));
                produtoService.decrementarEstoque(produto.getId(), item.getQuantidade());
                debitados.add(ItemPedidoEmbedded.de(produto, item.getQuantidade()));
            }
            salvo = repository.save(montarPedido(cliente, debitados, cupom, endereco, req.formaPagamento()));
        } catch (RuntimeException e) {
            devolverEstoque(debitados, e);
            throw e;
        }

        carrinhoRepository.deleteByClienteId(cliente.getId());
        return PedidoResponseDTO.from(salvo);
    }

    public PedidoResponseDTO buscarPorId(String id) {
        return PedidoResponseDTO.from(repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id)));
    }

    public PaginaResponseDTO<PedidoResponseDTO> listarPorCliente(String clienteId, int pagina, int tamanho) {
        if (!usuarioRepository.existsById(clienteId)) {
            throw new RecursoNaoEncontradoException("Cliente", clienteId);
        }
        PageRequest pageable = PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "criadoEm"));
        return PaginaResponseDTO.from(repository.findByClienteId(clienteId, pageable).map(PedidoResponseDTO::from));
    }

    private Pedido montarPedido(Usuario cliente, List<ItemPedidoEmbedded> itens, CupomAplicadoEmbedded cupom,
                                EnderecoEmbedded endereco, FormaPagamento formaPagamento) {
        BigDecimal subtotal = itens.stream()
                .map(ItemPedidoEmbedded::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal desconto = cupom == null
                ? BigDecimal.ZERO.setScale(2)
                : subtotal.multiply(cupom.getPercentualDesconto()).divide(CEM, 2, RoundingMode.HALF_UP);
        BigDecimal valorTotal = subtotal.subtract(desconto);
        LocalDateTime agora = LocalDateTime.now();

        return Pedido.builder()
                .clienteId(cliente.getId())
                .cliente(new ClienteSnapshotEmbedded(cliente.getNome(), cliente.getEmail()))
                .itens(List.copyOf(itens))
                .subtotal(subtotal)
                .desconto(desconto)
                .valorTotal(valorTotal)
                .cupom(cupom)
                .status(StatusPedido.AGUARDANDO_PAGAMENTO)
                .pagamento(PagamentoEmbedded.builder()
                        .forma(formaPagamento)
                        .status(StatusPagamento.PENDENTE)
                        .valor(valorTotal)
                        .atualizadoEm(agora)
                        .build())
                .entrega(EntregaEmbedded.builder()
                        .endereco(endereco)
                        .status(StatusEntrega.AGUARDANDO_ENVIO)
                        .build())
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build();
    }

    private CupomAplicadoEmbedded aplicarCupom(String codigo) {
        if (!StringUtils.hasText(codigo)) {
            return null;
        }
        CupomValidacaoResponse validacao = cupomService.validar(codigo);
        if (!validacao.valido()) {
            throw new CupomInvalidoException(validacao.mensagem());
        }
        return new CupomAplicadoEmbedded(codigo.trim().toUpperCase(), validacao.percentualDesconto());
    }

    private EnderecoEmbedded definirEndereco(EnderecoEntregaDTO informado, Usuario cliente) {
        if (informado != null) {
            return informado.toEmbedded();
        }
        List<EnderecoEmbedded> cadastrados = cliente.getEnderecos();
        if (cadastrados == null || cadastrados.isEmpty()) {
            throw new RequisicaoInvalidaException("Informe o endereço de entrega: o cliente não tem endereço cadastrado");
        }
        return EnderecoEntregaDTO.from(cadastrados.get(0)).toEmbedded();
    }

    private void devolverEstoque(List<ItemPedidoEmbedded> debitados, RuntimeException causa) {
        for (ItemPedidoEmbedded item : debitados) {
            try {
                produtoService.devolverEstoque(item.getProdutoId(), item.getQuantidade());
            } catch (RuntimeException e) {
                causa.addSuppressed(e);
            }
        }
    }

    private Usuario validarCliente(String clienteId) {
        Usuario usuario = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", clienteId));
        if (usuario.getRole() != Role.CLIENTE || !Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new ClienteInvalidoException(clienteId);
        }
        return usuario;
    }
}
