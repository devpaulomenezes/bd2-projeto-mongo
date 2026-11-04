package br.edu.ifpb.es.bd2;

import br.edu.ifpb.es.bd2.dto.LimpezaCarrinhoResponseDTO;
import br.edu.ifpb.es.bd2.model.Carrinho;
import br.edu.ifpb.es.bd2.model.ItemCarrinhoEmbedded;
import br.edu.ifpb.es.bd2.model.Produto;
import br.edu.ifpb.es.bd2.repository.CarrinhoRepository;
import br.edu.ifpb.es.bd2.repository.ProdutoRepository;
import br.edu.ifpb.es.bd2.service.CarrinhoLimpezaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarrinhoLimpezaServiceTest {

    @Mock
    private CarrinhoRepository carrinhoRepository;
    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private CarrinhoLimpezaService service;

    @Test
    @DisplayName("Deve remover carrinhos inativos usando a data limite calculada pelos dias de inatividade")
    void deveRemoverCarrinhosInativos() {
        when(carrinhoRepository.deleteByAtualizadoEmBefore(any())).thenReturn(4L);
        when(carrinhoRepository.findAll()).thenReturn(List.of());

        LimpezaCarrinhoResponseDTO r = service.executar(7);

        assertEquals(4, r.carrinhosInativosRemovidos());
        ArgumentCaptor<LocalDateTime> limite = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(carrinhoRepository).deleteByAtualizadoEmBefore(limite.capture());
        LocalDateTime esperado = LocalDateTime.now().minusDays(7);
        assertTrue(limite.getValue().isBefore(esperado.plusMinutes(1)) && limite.getValue().isAfter(esperado.minusMinutes(1)));
    }

    @Test
    @DisplayName("Deve remover do carrinho apenas os itens cujo produto não existe mais")
    void deveRemoverItensDeProdutosExcluidos() {
        Carrinho carrinho = carrinho("c-1", item("p-1"), item("p-2"));
        when(carrinhoRepository.findAll()).thenReturn(List.of(carrinho));
        when(produtoRepository.findAllById(any())).thenReturn(List.of(Produto.builder().id("p-1").build()));

        LimpezaCarrinhoResponseDTO r = service.executar(7);

        assertEquals(1, r.itensIndisponiveisRemovidos());
        assertEquals(0, r.carrinhosVaziosRemovidos());
        assertEquals(1, carrinho.getItens().size());
        assertEquals("p-1", carrinho.getItens().get(0).getProdutoId());
        verify(carrinhoRepository).save(carrinho);
        verify(carrinhoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve excluir o carrinho que ficar sem itens")
    void deveExcluirCarrinhoQueFicouVazio() {
        Carrinho carrinho = carrinho("c-1", item("p-9"));
        when(carrinhoRepository.findAll()).thenReturn(List.of(carrinho));
        when(produtoRepository.findAllById(any())).thenReturn(List.of());

        LimpezaCarrinhoResponseDTO r = service.executar(7);

        assertEquals(1, r.itensIndisponiveisRemovidos());
        assertEquals(1, r.carrinhosVaziosRemovidos());
        verify(carrinhoRepository).delete(carrinho);
        verify(carrinhoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve excluir carrinho que já estava vazio sem consultar produtos")
    void deveExcluirCarrinhoJaVazio() {
        Carrinho vazio = carrinho("c-1");
        when(carrinhoRepository.findAll()).thenReturn(List.of(vazio));

        LimpezaCarrinhoResponseDTO r = service.executar(7);

        assertEquals(0, r.itensIndisponiveisRemovidos());
        assertEquals(1, r.carrinhosVaziosRemovidos());
        verify(carrinhoRepository).delete(vazio);
        verify(produtoRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("Não deve gravar carrinho cujos produtos continuam no catálogo")
    void naoDeveAlterarCarrinhoValido() {
        Carrinho carrinho = carrinho("c-1", item("p-1"));
        when(carrinhoRepository.findAll()).thenReturn(List.of(carrinho));
        when(produtoRepository.findAllById(any())).thenReturn(List.of(Produto.builder().id("p-1").build()));

        LimpezaCarrinhoResponseDTO r = service.executar(7);

        assertEquals(0, r.itensIndisponiveisRemovidos());
        assertEquals(0, r.carrinhosVaziosRemovidos());
        verify(carrinhoRepository, never()).save(any());
        verify(carrinhoRepository, never()).delete(any());
    }

    private Carrinho carrinho(String clienteId, ItemCarrinhoEmbedded... itens) {
        Carrinho carrinho = new Carrinho(clienteId);
        carrinho.getItens().addAll(List.of(itens));
        return carrinho;
    }

    private ItemCarrinhoEmbedded item(String produtoId) {
        return new ItemCarrinhoEmbedded(produtoId, "Produto " + produtoId, new BigDecimal("10.00"), 1);
    }
}
