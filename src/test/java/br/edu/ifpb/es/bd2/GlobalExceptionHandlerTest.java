package br.edu.ifpb.es.bd2;

import br.edu.ifpb.es.bd2.exception.GlobalExceptionHandler;
import br.edu.ifpb.es.bd2.exception.RecursoNaoEncontradoException;
import br.edu.ifpb.es.bd2.exception.RequisicaoInvalidaException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControllerDeTeste())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Deve responder 404 no formato RFC 7807")
    void deveResponderNaoEncontrado() throws Exception {
        mockMvc.perform(get("/teste/nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.detail").value("Produto não encontrado(a): p-1"))
                .andExpect(jsonPath("$.instance").value("/teste/nao-encontrado"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Deve responder 400 com a mensagem da exceção de requisição inválida")
    void deveResponderRequisicaoInvalida() throws Exception {
        mockMvc.perform(get("/teste/requisicao-invalida"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requisição inválida"))
                .andExpect(jsonPath("$.detail").value("O carrinho está vazio"));
    }

    @Test
    @DisplayName("Deve listar os campos inválidos do Bean Validation")
    void deveListarCamposInvalidos() throws Exception {
        mockMvc.perform(post("/teste/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O nome é obrigatório"))
                .andExpect(jsonPath("$.campos[0].campo").value("nome"))
                .andExpect(jsonPath("$.campos[0].mensagem").value("O nome é obrigatório"));
    }

    @Test
    @DisplayName("Deve responder 400 para corpo mal formatado")
    void deveRejeitarCorpoMalFormatado() throws Exception {
        mockMvc.perform(post("/teste/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{nome"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O corpo da requisição está ausente ou mal formatado"));
    }

    @Test
    @DisplayName("Deve responder 409 quando o índice único do MongoDB é violado")
    void deveResponderConflitoDeIndiceUnico() throws Exception {
        mockMvc.perform(get("/teste/duplicado"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito"));
    }

    @Test
    @DisplayName("Deve responder 409 quando o documento foi alterado por outra operação")
    void deveResponderConflitoDeVersao() throws Exception {
        mockMvc.perform(get("/teste/versao"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("O registro foi alterado por outra operação. Recarregue os dados e tente novamente"));
    }

    @Test
    @DisplayName("Deve informar o parâmetro obrigatório ausente")
    void deveInformarParametroAusente() throws Exception {
        mockMvc.perform(get("/teste/parametro"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requisição inválida"))
                .andExpect(jsonPath("$.detail").value("O parâmetro 'clienteId' é obrigatório"));
    }

    @Test
    @DisplayName("Deve responder 500 sem expor detalhes internos")
    void deveEsconderDetalhesDeErroInesperado() throws Exception {
        mockMvc.perform(get("/teste/inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Ocorreu um erro inesperado. Tente novamente mais tarde."));
    }

    record CorpoDeTeste(@NotBlank(message = "O nome é obrigatório") String nome) {}

    @RestController
    @RequestMapping("/teste")
    static class ControllerDeTeste {

        @GetMapping("/nao-encontrado")
        void naoEncontrado() {
            throw new RecursoNaoEncontradoException("Produto", "p-1");
        }

        @GetMapping("/requisicao-invalida")
        void requisicaoInvalida() {
            throw new RequisicaoInvalidaException("O carrinho está vazio");
        }

        @PostMapping("/validacao")
        void validacao(@Valid @RequestBody CorpoDeTeste corpo) {
        }

        @GetMapping("/duplicado")
        void duplicado() {
            throw new DuplicateKeyException("E11000 duplicate key error");
        }

        @GetMapping("/versao")
        void versao() {
            throw new OptimisticLockingFailureException("versão desatualizada");
        }

        @GetMapping("/parametro")
        void parametro(@RequestParam("clienteId") String clienteId) {
        }

        @GetMapping("/inesperado")
        void inesperado() {
            throw new IllegalStateException("conexão interna recusada");
        }
    }
}
