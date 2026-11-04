package br.edu.ifpb.es.bd2;

import br.edu.ifpb.es.bd2.exception.EstoqueInsuficienteException;
import br.edu.ifpb.es.bd2.exception.RecursoNaoEncontradoException;
import br.edu.ifpb.es.bd2.model.Produto;
import br.edu.ifpb.es.bd2.model.Role;
import br.edu.ifpb.es.bd2.model.Usuario;
import br.edu.ifpb.es.bd2.repository.CategoriaRepository;
import br.edu.ifpb.es.bd2.repository.ProdutoRepository;
import br.edu.ifpb.es.bd2.repository.UsuarioRepository;
import br.edu.ifpb.es.bd2.service.ProdutoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercita Categoria e Produto contra um MongoDB real. So roda com BD2_TEST_MONGODB_URI definida;
 * cria dados com nomes unicos e remove apenas o que criou.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "BD2_TEST_MONGODB_URI", matches = ".+")
class CatalogoMongoIntegrationTest {

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> System.getenv("BD2_TEST_MONGODB_URI"));
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProdutoService produtoService;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    private final List<String> categoriaIds = new ArrayList<>();
    private final List<String> produtoIds = new ArrayList<>();
    private String categoriaId;
    private String vendedorId;

    @BeforeEach
    void criarDadosBase() throws Exception {
        categoriaId = criarCategoria(nomeUnico("Categoria"));

        Usuario vendedor = new Usuario();
        vendedor.setNome("Vendedor Teste");
        vendedor.setEmail(UUID.randomUUID() + "@teste.com");
        vendedor.setRole(Role.VENDEDOR);
        vendedorId = usuarioRepository.save(vendedor).getId();
    }

    @AfterEach
    void removerDadosCriados() {
        produtoRepository.deleteAllById(produtoIds);
        categoriaRepository.deleteAllById(categoriaIds);
        usuarioRepository.deleteById(vendedorId);
    }

    @Test
    @DisplayName("Deve executar o CRUD de categoria")
    void deveExecutarCrudDeCategoria() throws Exception {
        String nome = nomeUnico("Nova");
        String id = criarCategoria(nome);

        mockMvc.perform(get("/categorias/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(nome));

        String renomeada = nomeUnico("Renomeada");
        mockMvc.perform(put("/categorias/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", renomeada, "descricao", "Atualizada"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(renomeada));

        mockMvc.perform(delete("/categorias/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/categorias/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve rejeitar categoria com nome duplicado, sem diferenciar maiúsculas")
    void deveRejeitarCategoriaDuplicada() throws Exception {
        String nome = nomeUnico("Duplicada");
        criarCategoria(nome);

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", nome.toUpperCase()))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve propagar a renomeação da categoria apenas para os produtos dela")
    void devePropagarRenomeacaoDeCategoria() throws Exception {
        String primeiro = criarProduto("Caneta", "2.00", 1);
        String segundo = criarProduto("Lápis", "1.00", 1);
        String nomeOutra = nomeUnico("Outra");
        String outraCategoria = criarCategoria(nomeOutra);
        String deOutraCategoria = criar("/produtos", produtoRequest("Borracha", "1.50", 1, outraCategoria, vendedorId));
        produtoIds.add(deOutraCategoria);

        String renomeada = nomeUnico("Renomeada");
        mockMvc.perform(put("/categorias/{id}", categoriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", renomeada))))
                .andExpect(status().isOk());

        assertEquals(renomeada, produtoRepository.findById(primeiro).orElseThrow().getCategoriaNome());
        assertEquals(renomeada, produtoRepository.findById(segundo).orElseThrow().getCategoriaNome());
        assertEquals(nomeOutra, produtoRepository.findById(deOutraCategoria).orElseThrow().getCategoriaNome());
    }

    @Test
    @DisplayName("Deve bloquear a remoção de categoria com produtos e liberar quando não houver mais")
    void deveBloquearRemocaoDeCategoriaEmUso() throws Exception {
        String produto = criarProduto("Caderno", "20.00", 1);

        mockMvc.perform(delete("/categorias/{id}", categoriaId)).andExpect(status().isConflict());
        mockMvc.perform(get("/categorias/{id}", categoriaId)).andExpect(status().isOk());

        mockMvc.perform(delete("/produtos/{id}", produto)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/categorias/{id}", categoriaId)).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve rejeitar categoria sem nome")
    void deveRejeitarCategoriaSemNome() throws Exception {
        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", " "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O nome é obrigatório"));
    }

    @Test
    @DisplayName("Deve criar produto com dados desnormalizados e métricas zeradas")
    void deveCriarProduto() throws Exception {
        String id = criarProduto("Notebook", "4599.90", 3);

        mockMvc.perform(get("/produtos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preco").value(4599.90))
                .andExpect(jsonPath("$.categoriaId").value(categoriaId))
                .andExpect(jsonPath("$.vendedorNome").value("Vendedor Teste"))
                .andExpect(jsonPath("$.mediaAvaliacoes").value(0.0))
                .andExpect(jsonPath("$.totalAvaliacoes").value(0));
    }

    @Test
    @DisplayName("Deve filtrar por nome, categoria e faixa de preço numérica e inclusiva")
    void deveFiltrarProdutos() throws Exception {
        criarProduto("Mouse Gamer", "9.90", 1);
        String intermediario = criarProduto("Teclado Gamer", "100.00", 1);
        criarProduto("Monitor (4K)", "1000.00", 1);

        mockMvc.perform(get("/produtos").param("categoriaId", categoriaId))
                .andExpect(jsonPath("$", hasSize(3)));
        mockMvc.perform(get("/produtos").param("categoriaId", categoriaId).param("nome", "gAmEr"))
                .andExpect(jsonPath("$", hasSize(2)));
        mockMvc.perform(get("/produtos").param("categoriaId", categoriaId).param("nome", "(4k"))
                .andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/produtos").param("categoriaId", categoriaId)
                        .param("precoMin", "50").param("precoMax", "500"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(intermediario));
        mockMvc.perform(get("/produtos").param("categoriaId", categoriaId)
                        .param("precoMin", "100").param("precoMax", "100"))
                .andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/produtos").param("categoriaId", categoriaId).param("precoMin", "100"))
                .andExpect(jsonPath("$", hasSize(2)));
        mockMvc.perform(get("/produtos").param("categoriaId", categoriaId).param("precoMax", "100"))
                .andExpect(jsonPath("$", hasSize(2)));
        mockMvc.perform(get("/produtos").param("nome", intermediario))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Deve atualizar produto preservando as métricas de avaliação")
    void deveAtualizarProdutoPreservandoMetricas() throws Exception {
        String id = criarProduto("Webcam", "200.00", 2);
        produtoService.atualizarMetricasAvaliacao(id, 4.5, 2);
        String outraCategoria = criarCategoria(nomeUnico("Outra"));

        mockMvc.perform(put("/produtos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(produtoRequest("Webcam HD", "250.00", 7, outraCategoria, vendedorId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Webcam HD"))
                .andExpect(jsonPath("$.preco").value(250.00))
                .andExpect(jsonPath("$.estoque").value(7))
                .andExpect(jsonPath("$.categoriaId").value(outraCategoria))
                .andExpect(jsonPath("$.mediaAvaliacoes").value(4.5))
                .andExpect(jsonPath("$.totalAvaliacoes").value(2));
    }

    @Test
    @DisplayName("Deve validar o corpo e retornar 404 para produto inexistente")
    void deveTratarErrosDeProduto() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(produtoRequest("Item", "0", 1, categoriaId, vendedorId))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(produtoRequest("Item", "1.00", 1, "inexistente", vendedorId))))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/produtos/{id}", "inexistente")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve remover produto")
    void deveRemoverProduto() throws Exception {
        String id = criarProduto("Cabo", "15.00", 1);

        mockMvc.perform(delete("/produtos/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/produtos/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve decrementar estoque e rejeitar retirada acima do disponível")
    void deveDecrementarEstoque() throws Exception {
        String id = criarProduto("Headset", "300.00", 5);

        produtoService.decrementarEstoque(id, 2);
        assertEquals(3, estoqueDe(id));

        assertThrows(EstoqueInsuficienteException.class, () -> produtoService.decrementarEstoque(id, 4));
        assertEquals(3, estoqueDe(id));

        assertThrows(RecursoNaoEncontradoException.class, () -> produtoService.decrementarEstoque("inexistente", 1));
    }

    @Test
    @DisplayName("Não deve vender além do estoque em retiradas concorrentes")
    void naoDeveVenderAlemDoEstoqueEmConcorrencia() throws Exception {
        String id = criarProduto("Console", "2500.00", 10);
        AtomicInteger vendas = new AtomicInteger();
        AtomicInteger recusas = new AtomicInteger();

        try (ExecutorService executor = Executors.newFixedThreadPool(8)) {
            for (int i = 0; i < 30; i++) {
                executor.submit(() -> {
                    try {
                        produtoService.decrementarEstoque(id, 1);
                        vendas.incrementAndGet();
                    } catch (EstoqueInsuficienteException e) {
                        recusas.incrementAndGet();
                    }
                });
            }
        }

        assertEquals(10, vendas.get());
        assertEquals(20, recusas.get());
        assertEquals(0, estoqueDe(id));
    }

    @Test
    @DisplayName("Deve atualizar métricas de avaliação e rejeitar produto inexistente")
    void deveAtualizarMetricas() throws Exception {
        String id = criarProduto("Tablet", "1500.00", 1);

        produtoService.atualizarMetricasAvaliacao(id, 3.5, 4);

        Produto produto = produtoRepository.findById(id).orElseThrow();
        assertEquals(3.5, produto.getMediaAvaliacoes());
        assertEquals(4, produto.getTotalAvaliacoes());
        assertThrows(RecursoNaoEncontradoException.class,
                () -> produtoService.atualizarMetricasAvaliacao("inexistente", 1.0, 1));
    }

    @Test
    @DisplayName("Deve documentar as rotas de produto no OpenAPI")
    void deveDocumentarRotasDeProduto() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/produtos'].get.parameters", hasSize(4)))
                .andExpect(jsonPath("$.paths['/produtos/{id}'].put").exists());
    }

    private int estoqueDe(String id) {
        return produtoRepository.findById(id).orElseThrow().getEstoque();
    }

    private String criarCategoria(String nome) throws Exception {
        String id = criar("/categorias", Map.of("nome", nome, "descricao", "Teste"));
        categoriaIds.add(id);
        return id;
    }

    private String criarProduto(String nome, String preco, int estoque) throws Exception {
        String id = criar("/produtos", produtoRequest(nome, preco, estoque, categoriaId, vendedorId));
        produtoIds.add(id);
        return id;
    }

    private String criar(String rota, Map<String, Object> corpo) throws Exception {
        String resposta = mockMvc.perform(post(rota)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corpo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(resposta);
        return node.get("id").asText();
    }

    private Map<String, Object> produtoRequest(String nome, String preco, int estoque, String categoria, String vendedor) {
        return Map.of("nome", nome, "descricao", "Teste", "preco", preco, "estoque", estoque,
                "categoriaId", categoria, "vendedorId", vendedor);
    }

    private String nomeUnico(String prefixo) {
        return prefixo + "-" + UUID.randomUUID();
    }

    private String json(Map<String, ?> corpo) throws Exception {
        return objectMapper.writeValueAsString(corpo);
    }
}
