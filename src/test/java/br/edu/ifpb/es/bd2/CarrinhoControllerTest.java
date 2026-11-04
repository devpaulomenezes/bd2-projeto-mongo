package br.edu.ifpb.es.bd2;

import br.edu.ifpb.es.bd2.controller.CarrinhoController;
import br.edu.ifpb.es.bd2.dto.CarrinhoResponseDTO;
import br.edu.ifpb.es.bd2.dto.ItemCarrinhoResponseDTO;
import br.edu.ifpb.es.bd2.exception.GlobalExceptionHandler;
import br.edu.ifpb.es.bd2.exception.RecursoNaoEncontradoException;
import br.edu.ifpb.es.bd2.service.CarrinhoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarrinhoController.class)
@Import(GlobalExceptionHandler.class)
class CarrinhoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarrinhoService service;

    private CarrinhoResponseDTO carrinho(int quantidade) {
        BigDecimal preco = new BigDecimal("100.00");
        ItemCarrinhoResponseDTO item = new ItemCarrinhoResponseDTO("p-1", "Notebook", preco, quantidade,
                preco.multiply(BigDecimal.valueOf(quantidade)));
        return new CarrinhoResponseDTO("car-1", "c-1", List.of(item), item.subtotal());
    }

    @Test
    @DisplayName("GET deve retornar o carrinho do cliente")
    void deveBuscarCarrinho() throws Exception {
        when(service.buscar("c-1")).thenReturn(carrinho(2));

        mockMvc.perform(get("/carrinhos/c-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value("c-1"))
                .andExpect(jsonPath("$.itens[0].produtoId").value("p-1"))
                .andExpect(jsonPath("$.valorTotal").value(200.00));
    }

    @Test
    @DisplayName("POST deve adicionar o item e retornar 201")
    void deveAdicionarItem() throws Exception {
        when(service.adicionarItem(eq("c-1"), any())).thenReturn(carrinho(2));

        mockMvc.perform(post("/carrinhos/c-1/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produtoId\":\"p-1\",\"quantidade\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itens[0].quantidade").value(2));
    }

    @Test
    @DisplayName("POST deve rejeitar quantidade menor que 1 com 400")
    void deveRejeitarQuantidadeInvalidaNoPost() throws Exception {
        mockMvc.perform(post("/carrinhos/c-1/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produtoId\":\"p-1\",\"quantidade\":0}"))
                .andExpect(status().isBadRequest());
        verify(service, never()).adicionarItem(any(), any());
    }

    @Test
    @DisplayName("POST deve rejeitar produto em branco com 400")
    void deveRejeitarProdutoEmBranco() throws Exception {
        mockMvc.perform(post("/carrinhos/c-1/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produtoId\":\"\",\"quantidade\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT deve alterar a quantidade do item")
    void deveAlterarQuantidade() throws Exception {
        when(service.alterarQuantidade("c-1", "p-1", 5)).thenReturn(carrinho(5));

        mockMvc.perform(put("/carrinhos/c-1/itens/p-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].quantidade").value(5));
    }

    @Test
    @DisplayName("PUT deve rejeitar quantidade ausente com 400")
    void deveRejeitarQuantidadeAusenteNoPut() throws Exception {
        mockMvc.perform(put("/carrinhos/c-1/itens/p-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE deve remover o item e retornar o carrinho atualizado")
    void deveRemoverItem() throws Exception {
        when(service.removerItem("c-1", "p-1"))
                .thenReturn(new CarrinhoResponseDTO("car-1", "c-1", List.of(), BigDecimal.ZERO));

        mockMvc.perform(delete("/carrinhos/c-1/itens/p-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens").isEmpty());
    }

    @Test
    @DisplayName("DELETE de item inexistente deve retornar 404")
    void deveRetornar404AoRemoverItemAusente() throws Exception {
        when(service.removerItem("c-1", "p-9"))
                .thenThrow(new RecursoNaoEncontradoException("Item do carrinho (produto)", "p-9"));

        mockMvc.perform(delete("/carrinhos/c-1/itens/p-9"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE do carrinho deve esvaziá-lo e retornar o carrinho vazio")
    void deveLimparCarrinho() throws Exception {
        when(service.limpar("c-1"))
                .thenReturn(new CarrinhoResponseDTO(null, "c-1", List.of(), BigDecimal.ZERO));

        mockMvc.perform(delete("/carrinhos/c-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value("c-1"))
                .andExpect(jsonPath("$.itens").isEmpty());
    }
}
