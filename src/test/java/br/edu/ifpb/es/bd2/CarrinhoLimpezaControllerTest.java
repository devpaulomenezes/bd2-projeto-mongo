package br.edu.ifpb.es.bd2;

import br.edu.ifpb.es.bd2.controller.CarrinhoLimpezaController;
import br.edu.ifpb.es.bd2.dto.LimpezaCarrinhoResponseDTO;
import br.edu.ifpb.es.bd2.exception.GlobalExceptionHandler;
import br.edu.ifpb.es.bd2.service.CarrinhoLimpezaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarrinhoLimpezaController.class)
@Import(GlobalExceptionHandler.class)
class CarrinhoLimpezaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarrinhoLimpezaService service;

    @Test
    @DisplayName("POST deve executar a limpeza e retornar o resumo")
    void deveExecutarLimpeza() throws Exception {
        when(service.executar()).thenReturn(new LimpezaCarrinhoResponseDTO(3, 2, 1, LocalDateTime.now()));

        mockMvc.perform(post("/carrinhos/limpeza"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carrinhosInativosRemovidos").value(3))
                .andExpect(jsonPath("$.itensIndisponiveisRemovidos").value(2))
                .andExpect(jsonPath("$.carrinhosVaziosRemovidos").value(1));
    }
}
