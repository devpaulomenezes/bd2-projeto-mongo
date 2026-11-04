package br.edu.ifpb.es.bd2;

import br.edu.ifpb.es.bd2.dto.CarrinhoResponseDTO;
import br.edu.ifpb.es.bd2.dto.ItemCarrinhoRequestDTO;
import br.edu.ifpb.es.bd2.exception.ClienteInvalidoException;
import br.edu.ifpb.es.bd2.exception.EstoqueInsuficienteException;
import br.edu.ifpb.es.bd2.exception.RecursoNaoEncontradoException;
import br.edu.ifpb.es.bd2.model.Carrinho;
import br.edu.ifpb.es.bd2.model.ItemCarrinhoEmbedded;
import br.edu.ifpb.es.bd2.model.Produto;
import br.edu.ifpb.es.bd2.model.Role;
import br.edu.ifpb.es.bd2.model.Usuario;
import br.edu.ifpb.es.bd2.repository.CarrinhoRepository;
import br.edu.ifpb.es.bd2.repository.ProdutoRepository;
import br.edu.ifpb.es.bd2.repository.UsuarioRepository;
import br.edu.ifpb.es.bd2.service.CarrinhoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarrinhoServiceTest {

    @Mock
    private CarrinhoRepository repository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CarrinhoService service;

    @Test
    @DisplayName("Deve criar o carrinho e registrar nome e preço do produto ao adicionar o primeiro item")
    void deveCriarCarrinhoAoAdicionarPrimeiroItem() {
        clienteValido();
        when(produtoRepository.findById("p-1")).thenReturn(Optional.of(produto(10)));
        when(repository.findByClienteId("c-1")).thenReturn(Optional.empty());
        when(repository.save(any(Carrinho.class))).thenAnswer(i -> i.getArgument(0));

        CarrinhoResponseDTO carrinho = service.adicionarItem("c-1", new ItemCarrinhoRequestDTO("p-1", 2));

        assertEquals("c-1", carrinho.clienteId());
        assertEquals(1, carrinho.itens().size());
        assertEquals("Notebook", carrinho.itens().get(0).nome());
        assertEquals(2, carrinho.itens().get(0).quantidade());
        assertEquals(0, new BigDecimal("200.00").compareTo(carrinho.valorTotal()));
    }

    @Test
    @DisplayName("Deve somar a quantidade quando o produto já está no carrinho")
    void deveSomarQuantidadeDeProdutoExistente() {
        clienteValido();
        when(produtoRepository.findById("p-1")).thenReturn(Optional.of(produto(10)));
        when(repository.findByClienteId("c-1")).thenReturn(Optional.of(carrinhoComItem(3)));
        when(repository.save(any(Carrinho.class))).thenAnswer(i -> i.getArgument(0));

        CarrinhoResponseDTO carrinho = service.adicionarItem("c-1", new ItemCarrinhoRequestDTO("p-1", 2));

        assertEquals(1, carrinho.itens().size());
        assertEquals(5, carrinho.itens().get(0).quantidade());
    }

    @Test
    @DisplayName("Deve rejeitar adição acima do estoque sem gravar o carrinho")
    void deveRejeitarAdicaoAcimaDoEstoque() {
        clienteValido();
        when(produtoRepository.findById("p-1")).thenReturn(Optional.of(produto(4)));
        when(repository.findByClienteId("c-1")).thenReturn(Optional.of(carrinhoComItem(3)));

        assertThrows(EstoqueInsuficienteException.class,
                () -> service.adicionarItem("c-1", new ItemCarrinhoRequestDTO("p-1", 2)));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar produto inexistente")
    void deveRejeitarProdutoInexistente() {
        clienteValido();
        when(produtoRepository.findById("p-1")).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.adicionarItem("c-1", new ItemCarrinhoRequestDTO("p-1", 1)));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar usuário que não é cliente")
    void deveRejeitarUsuarioQueNaoECliente() {
        when(usuarioRepository.findById("c-1")).thenReturn(Optional.of(usuario(Role.VENDEDOR, true)));

        assertThrows(ClienteInvalidoException.class, () -> service.buscar("c-1"));
    }

    @Test
    @DisplayName("Deve rejeitar cliente inativo")
    void deveRejeitarClienteInativo() {
        when(usuarioRepository.findById("c-1")).thenReturn(Optional.of(usuario(Role.CLIENTE, false)));

        assertThrows(ClienteInvalidoException.class, () -> service.buscar("c-1"));
    }

    @Test
    @DisplayName("Deve retornar carrinho vazio para cliente sem carrinho")
    void deveRetornarCarrinhoVazio() {
        clienteValido();
        when(repository.findByClienteId("c-1")).thenReturn(Optional.empty());

        CarrinhoResponseDTO carrinho = service.buscar("c-1");

        assertTrue(carrinho.itens().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(carrinho.valorTotal()));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve substituir a quantidade ao alterar um item")
    void deveAlterarQuantidade() {
        clienteValido();
        when(repository.findByClienteId("c-1")).thenReturn(Optional.of(carrinhoComItem(3)));
        when(produtoRepository.findById("p-1")).thenReturn(Optional.of(produto(10)));
        when(repository.save(any(Carrinho.class))).thenAnswer(i -> i.getArgument(0));

        CarrinhoResponseDTO carrinho = service.alterarQuantidade("c-1", "p-1", 7);

        assertEquals(7, carrinho.itens().get(0).quantidade());
        assertEquals(0, new BigDecimal("700.00").compareTo(carrinho.valorTotal()));
    }

    @Test
    @DisplayName("Deve rejeitar alteração acima do estoque")
    void deveRejeitarAlteracaoAcimaDoEstoque() {
        clienteValido();
        when(repository.findByClienteId("c-1")).thenReturn(Optional.of(carrinhoComItem(3)));
        when(produtoRepository.findById("p-1")).thenReturn(Optional.of(produto(5)));

        assertThrows(EstoqueInsuficienteException.class, () -> service.alterarQuantidade("c-1", "p-1", 6));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar alteração de item que não está no carrinho")
    void deveRejeitarAlteracaoDeItemAusente() {
        clienteValido();
        when(repository.findByClienteId("c-1")).thenReturn(Optional.of(carrinhoComItem(3)));

        assertThrows(RecursoNaoEncontradoException.class, () -> service.alterarQuantidade("c-1", "p-2", 1));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve remover o item mesmo que o produto não exista mais no catálogo")
    void deveRemoverItem() {
        clienteValido();
        when(repository.findByClienteId("c-1")).thenReturn(Optional.of(carrinhoComItem(3)));
        when(repository.save(any(Carrinho.class))).thenAnswer(i -> i.getArgument(0));

        CarrinhoResponseDTO carrinho = service.removerItem("c-1", "p-1");

        assertTrue(carrinho.itens().isEmpty());
        verify(produtoRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve rejeitar remoção de item que não está no carrinho")
    void deveRejeitarRemocaoDeItemAusente() {
        clienteValido();
        when(repository.findByClienteId("c-1")).thenReturn(Optional.of(carrinhoComItem(3)));

        assertThrows(RecursoNaoEncontradoException.class, () -> service.removerItem("c-1", "p-2"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar remoção quando o cliente não tem carrinho")
    void deveRejeitarRemocaoSemCarrinho() {
        clienteValido();
        when(repository.findByClienteId("c-1")).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.removerItem("c-1", "p-1"));
    }

    @Test
    @DisplayName("Deve esvaziar o carrinho do cliente e retornar carrinho vazio")
    void deveLimparCarrinho() {
        clienteValido();

        CarrinhoResponseDTO carrinho = service.limpar("c-1");

        assertTrue(carrinho.itens().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(carrinho.valorTotal()));
        verify(repository).deleteByClienteId("c-1");
    }

    @Test
    @DisplayName("Deve rejeitar limpeza de carrinho de usuário que não é cliente")
    void deveRejeitarLimpezaDeNaoCliente() {
        when(usuarioRepository.findById("c-1")).thenReturn(Optional.of(usuario(Role.VENDEDOR, true)));

        assertThrows(ClienteInvalidoException.class, () -> service.limpar("c-1"));
        verify(repository, never()).deleteByClienteId(any());
    }

    @Test
    @DisplayName("Deve atualizar a data de atividade do carrinho ao adicionar item")
    void deveAtualizarAtividadeAoAdicionar() {
        clienteValido();
        when(produtoRepository.findById("p-1")).thenReturn(Optional.of(produto(10)));
        Carrinho existente = carrinhoComItem(1);
        existente.setAtualizadoEm(java.time.LocalDateTime.now().minusDays(30));
        when(repository.findByClienteId("c-1")).thenReturn(Optional.of(existente));
        when(repository.save(any(Carrinho.class))).thenAnswer(i -> i.getArgument(0));

        service.adicionarItem("c-1", new ItemCarrinhoRequestDTO("p-1", 1));

        assertTrue(existente.getAtualizadoEm().isAfter(java.time.LocalDateTime.now().minusMinutes(1)));
    }

    private void clienteValido() {
        when(usuarioRepository.findById("c-1")).thenReturn(Optional.of(usuario(Role.CLIENTE, true)));
    }

    private Usuario usuario(Role role, boolean ativo) {
        Usuario usuario = new Usuario();
        usuario.setId("c-1");
        usuario.setNome("Cliente Maria");
        usuario.setRole(role);
        usuario.setAtivo(ativo);
        return usuario;
    }

    private Produto produto(int estoque) {
        return Produto.builder().id("p-1").nome("Notebook").preco(new BigDecimal("100.00")).estoque(estoque).build();
    }

    private Carrinho carrinhoComItem(int quantidade) {
        Carrinho carrinho = new Carrinho("c-1");
        carrinho.setId("car-1");
        carrinho.getItens().add(new ItemCarrinhoEmbedded("p-1", "Notebook", new BigDecimal("100.00"), quantidade));
        return carrinho;
    }
}
