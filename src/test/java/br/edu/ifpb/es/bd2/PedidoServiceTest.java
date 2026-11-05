package br.edu.ifpb.es.bd2;

import br.edu.ifpb.es.bd2.dto.CheckoutRequestDTO;
import br.edu.ifpb.es.bd2.dto.CupomValidacaoResponse;
import br.edu.ifpb.es.bd2.dto.EnderecoEntregaDTO;
import br.edu.ifpb.es.bd2.dto.PaginaResponseDTO;
import br.edu.ifpb.es.bd2.dto.PedidoResponseDTO;
import br.edu.ifpb.es.bd2.exception.CarrinhoVazioException;
import br.edu.ifpb.es.bd2.exception.ClienteInvalidoException;
import br.edu.ifpb.es.bd2.exception.CupomInvalidoException;
import br.edu.ifpb.es.bd2.exception.EstoqueInsuficienteException;
import br.edu.ifpb.es.bd2.exception.RecursoNaoEncontradoException;
import br.edu.ifpb.es.bd2.exception.RequisicaoInvalidaException;
import br.edu.ifpb.es.bd2.model.Carrinho;
import br.edu.ifpb.es.bd2.model.EnderecoEmbedded;
import br.edu.ifpb.es.bd2.model.FormaPagamento;
import br.edu.ifpb.es.bd2.model.ItemCarrinhoEmbedded;
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
import br.edu.ifpb.es.bd2.service.CupomService;
import br.edu.ifpb.es.bd2.service.PedidoService;
import br.edu.ifpb.es.bd2.service.ProdutoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository repository;
    @Mock
    private CarrinhoRepository carrinhoRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ProdutoService produtoService;
    @Mock
    private CupomService cupomService;

    @InjectMocks
    private PedidoService service;

    @Test
    @DisplayName("Deve finalizar a compra com o preço atual do catálogo, aplicar o cupom e esvaziar o carrinho")
    void deveFinalizarCompraComCupom() {
        clienteValido();
        when(carrinhoRepository.findByClienteId("c-1")).thenReturn(Optional.of(carrinho()));
        when(cupomService.validar("bemvindo10"))
                .thenReturn(new CupomValidacaoResponse(true, "Cupom válido", new BigDecimal("10")));
        produtosNoCatalogo();
        when(repository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        PedidoResponseDTO pedido = service.checkout(
                new CheckoutRequestDTO("c-1", "bemvindo10", FormaPagamento.PIX, null));

        assertEquals(StatusPedido.AGUARDANDO_PAGAMENTO, pedido.status());
        assertEquals(0, new BigDecimal("120.00").compareTo(pedido.itens().get(0).precoUnitario()));
        assertEquals(0, new BigDecimal("240.00").compareTo(pedido.itens().get(0).subtotal()));
        assertEquals(0, new BigDecimal("290.00").compareTo(pedido.subtotal()));
        assertEquals(0, new BigDecimal("29.00").compareTo(pedido.desconto()));
        assertEquals(0, new BigDecimal("261.00").compareTo(pedido.valorTotal()));
        assertEquals("BEMVINDO10", pedido.cupom().codigo());
        assertEquals(FormaPagamento.PIX, pedido.pagamento().forma());
        assertEquals(StatusPagamento.PENDENTE, pedido.pagamento().status());
        assertEquals(0, new BigDecimal("261.00").compareTo(pedido.pagamento().valor()));
        assertEquals(StatusEntrega.AGUARDANDO_ENVIO, pedido.entrega().status());
        assertEquals("Rua das Flores", pedido.entrega().endereco().logradouro());
        assertEquals("Cliente Maria", pedido.cliente().nome());
        assertEquals("maria@email.com", pedido.cliente().email());

        verify(produtoService).decrementarEstoque("p-1", 2);
        verify(produtoService).decrementarEstoque("p-2", 2);
        verify(produtoService, never()).devolverEstoque(any(), anyInt());
        InOrder ordem = inOrder(repository, carrinhoRepository);
        ordem.verify(repository).save(any(Pedido.class));
        ordem.verify(carrinhoRepository).deleteByClienteId("c-1");
    }

    @Test
    @DisplayName("Deve manter os dados do pedido mesmo que produto e cliente mudem depois do checkout")
    void deveCongelarDadosDoPedido() {
        Usuario cliente = cliente();
        when(usuarioRepository.findById("c-1")).thenReturn(Optional.of(cliente));
        when(carrinhoRepository.findByClienteId("c-1")).thenReturn(Optional.of(carrinho()));
        Produto notebook = produto("p-1", "Notebook", "120.00");
        when(produtoRepository.findById("p-1")).thenReturn(Optional.of(notebook));
        when(produtoRepository.findById("p-2")).thenReturn(Optional.of(produto("p-2", "Mouse", "25.00")));
        when(repository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        service.checkout(checkout(null));

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(repository).save(captor.capture());
        Pedido pedido = captor.getValue();

        notebook.setPreco(new BigDecimal("999.00"));
        notebook.setNome("Notebook Gamer");
        cliente.setNome("Maria Souza");
        cliente.getEnderecos().get(0).setLogradouro("Rua Nova");

        assertEquals(0, new BigDecimal("120.00").compareTo(pedido.getItens().get(0).getPrecoUnitario()));
        assertEquals("Notebook", pedido.getItens().get(0).getNome());
        assertEquals("Cliente Maria", pedido.getCliente().getNome());
        assertEquals("Rua das Flores", pedido.getEntrega().getEndereco().getLogradouro());
        assertThrows(UnsupportedOperationException.class, () -> pedido.getItens().clear());
    }

    @Test
    @DisplayName("Deve usar o endereço informado no checkout no lugar do cadastrado")
    void deveUsarEnderecoInformado() {
        clienteValido();
        when(carrinhoRepository.findByClienteId("c-1")).thenReturn(Optional.of(carrinho()));
        produtosNoCatalogo();
        when(repository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));
        EnderecoEntregaDTO outro = new EnderecoEntregaDTO(
                "Av. Epitácio Pessoa", "500", "Tambaú", "João Pessoa", "PB", "58039-000");

        PedidoResponseDTO pedido = service.checkout(
                new CheckoutRequestDTO("c-1", null, FormaPagamento.BOLETO, outro));

        assertEquals("Av. Epitácio Pessoa", pedido.entrega().endereco().logradouro());
        assertNull(pedido.cupom());
        assertEquals(0, BigDecimal.ZERO.compareTo(pedido.desconto()));
        assertEquals(0, new BigDecimal("290.00").compareTo(pedido.valorTotal()));
        verify(cupomService, never()).validar(any());
    }

    @Test
    @DisplayName("Deve rejeitar checkout com carrinho vazio")
    void deveRejeitarCarrinhoVazio() {
        clienteValido();
        when(carrinhoRepository.findByClienteId("c-1")).thenReturn(Optional.of(new Carrinho("c-1")));

        assertThrows(CarrinhoVazioException.class, () -> service.checkout(checkout(null)));
        verify(produtoService, never()).decrementarEstoque(any(), anyInt());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar checkout de cliente sem carrinho")
    void deveRejeitarClienteSemCarrinho() {
        clienteValido();
        when(carrinhoRepository.findByClienteId("c-1")).thenReturn(Optional.empty());

        assertThrows(CarrinhoVazioException.class, () -> service.checkout(checkout(null)));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar cupom inválido sem mexer no estoque nem no carrinho")
    void deveRejeitarCupomInvalido() {
        clienteValido();
        when(carrinhoRepository.findByClienteId("c-1")).thenReturn(Optional.of(carrinho()));
        when(cupomService.validar("VENCIDO")).thenReturn(new CupomValidacaoResponse(false, "Cupom expirado", null));

        CupomInvalidoException erro = assertThrows(CupomInvalidoException.class,
                () -> service.checkout(checkout("VENCIDO")));

        assertEquals("Cupom expirado", erro.getMessage());
        verify(produtoService, never()).decrementarEstoque(any(), anyInt());
        verify(repository, never()).save(any());
        verify(carrinhoRepository, never()).deleteByClienteId(any());
    }

    @Test
    @DisplayName("Deve devolver o estoque já debitado quando falta estoque em outro item")
    void deveDevolverEstoqueQuandoFaltaEmOutroItem() {
        clienteValido();
        when(carrinhoRepository.findByClienteId("c-1")).thenReturn(Optional.of(carrinho()));
        produtosNoCatalogo();
        doNothing().when(produtoService).decrementarEstoque("p-1", 2);
        doThrow(new EstoqueInsuficienteException("p-2", 2)).when(produtoService).decrementarEstoque("p-2", 2);

        assertThrows(EstoqueInsuficienteException.class, () -> service.checkout(checkout(null)));

        verify(produtoService).devolverEstoque("p-1", 2);
        verify(produtoService, never()).devolverEstoque(eq("p-2"), anyInt());
        verify(repository, never()).save(any());
        verify(carrinhoRepository, never()).deleteByClienteId(any());
    }

    @Test
    @DisplayName("Deve devolver o estoque quando um produto do carrinho saiu do catálogo")
    void deveDevolverEstoqueQuandoProdutoFoiRemovido() {
        clienteValido();
        when(carrinhoRepository.findByClienteId("c-1")).thenReturn(Optional.of(carrinho()));
        when(produtoRepository.findById("p-1")).thenReturn(Optional.of(produto("p-1", "Notebook", "120.00")));
        when(produtoRepository.findById("p-2")).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.checkout(checkout(null)));

        verify(produtoService).devolverEstoque("p-1", 2);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve devolver todo o estoque quando o pedido não consegue ser gravado")
    void deveDevolverEstoqueQuandoFalhaAoGravar() {
        clienteValido();
        when(carrinhoRepository.findByClienteId("c-1")).thenReturn(Optional.of(carrinho()));
        produtosNoCatalogo();
        when(repository.save(any(Pedido.class))).thenThrow(new DataAccessResourceFailureException("MongoDB indisponível"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.checkout(checkout(null)));

        verify(produtoService).devolverEstoque("p-1", 2);
        verify(produtoService).devolverEstoque("p-2", 2);
        verify(carrinhoRepository, never()).deleteByClienteId(any());
    }

    @Test
    @DisplayName("Deve exigir endereço quando o cliente não tem nenhum cadastrado")
    void deveExigirEnderecoDeEntrega() {
        Usuario cliente = cliente();
        cliente.setEnderecos(List.of());
        when(usuarioRepository.findById("c-1")).thenReturn(Optional.of(cliente));
        when(carrinhoRepository.findByClienteId("c-1")).thenReturn(Optional.of(carrinho()));

        assertThrows(RequisicaoInvalidaException.class, () -> service.checkout(checkout(null)));
        verify(produtoService, never()).decrementarEstoque(any(), anyInt());
    }

    @Test
    @DisplayName("Deve rejeitar checkout de usuário que não é cliente")
    void deveRejeitarUsuarioQueNaoECliente() {
        Usuario vendedor = cliente();
        vendedor.setRole(Role.VENDEDOR);
        when(usuarioRepository.findById("c-1")).thenReturn(Optional.of(vendedor));

        assertThrows(ClienteInvalidoException.class, () -> service.checkout(checkout(null)));
        verify(carrinhoRepository, never()).findByClienteId(any());
    }

    @Test
    @DisplayName("Deve listar os pedidos do cliente em páginas, do mais recente para o mais antigo")
    void deveListarPedidosDoCliente() {
        when(usuarioRepository.existsById("c-1")).thenReturn(true);
        Pedido pedido = Pedido.builder().id("ped-1").clienteId("c-1").itens(List.of()).status(StatusPedido.PAGO).build();
        when(repository.findByClienteId(eq("c-1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(pedido), PageRequest.of(0, 5), 1));

        PaginaResponseDTO<PedidoResponseDTO> pagina = service.listarPorCliente("c-1", 0, 5);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findByClienteId(eq("c-1"), captor.capture());
        assertEquals(Sort.Direction.DESC, captor.getValue().getSort().getOrderFor("criadoEm").getDirection());
        assertEquals(1, pagina.conteudo().size());
        assertEquals("ped-1", pagina.conteudo().get(0).id());
        assertEquals(StatusPedido.PAGO, pagina.conteudo().get(0).status());
    }

    @Test
    @DisplayName("Deve responder não encontrado ao listar pedidos de cliente inexistente")
    void deveRejeitarListagemDeClienteInexistente() {
        when(usuarioRepository.existsById("c-9")).thenReturn(false);

        assertThrows(RecursoNaoEncontradoException.class, () -> service.listarPorCliente("c-9", 0, 10));
    }

    @Test
    @DisplayName("Deve responder não encontrado para pedido inexistente")
    void deveRejeitarPedidoInexistente() {
        when(repository.findById("ped-9")).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorId("ped-9"));
    }

    private void clienteValido() {
        when(usuarioRepository.findById("c-1")).thenReturn(Optional.of(cliente()));
    }

    private void produtosNoCatalogo() {
        when(produtoRepository.findById("p-1")).thenReturn(Optional.of(produto("p-1", "Notebook", "120.00")));
        when(produtoRepository.findById("p-2")).thenReturn(Optional.of(produto("p-2", "Mouse", "25.00")));
    }

    private CheckoutRequestDTO checkout(String cupom) {
        return new CheckoutRequestDTO("c-1", cupom, FormaPagamento.PIX, null);
    }

    private Usuario cliente() {
        EnderecoEmbedded endereco = new EnderecoEmbedded();
        endereco.setLogradouro("Rua das Flores");
        endereco.setNumero("120");
        endereco.setBairro("Centro");
        endereco.setCidade("João Pessoa");
        endereco.setEstado("PB");
        endereco.setCep("58010-000");

        Usuario usuario = new Usuario();
        usuario.setId("c-1");
        usuario.setNome("Cliente Maria");
        usuario.setEmail("maria@email.com");
        usuario.setRole(Role.CLIENTE);
        usuario.setAtivo(true);
        usuario.setEnderecos(List.of(endereco));
        return usuario;
    }

    private Produto produto(String id, String nome, String preco) {
        return Produto.builder().id(id).nome(nome).preco(new BigDecimal(preco)).estoque(10).build();
    }

    private Carrinho carrinho() {
        Carrinho carrinho = new Carrinho("c-1");
        carrinho.getItens().add(new ItemCarrinhoEmbedded("p-1", "Notebook", new BigDecimal("100.00"), 2));
        carrinho.getItens().add(new ItemCarrinhoEmbedded("p-2", "Mouse", new BigDecimal("40.00"), 2));
        return carrinho;
    }
}
