package br.edu.ifpb.es.bd2;

import br.edu.ifpb.es.bd2.dto.ProdutoRequestDTO;
import br.edu.ifpb.es.bd2.dto.ProdutoResponseDTO;
import br.edu.ifpb.es.bd2.exception.RecursoNaoEncontradoException;
import br.edu.ifpb.es.bd2.exception.VendedorInvalidoException;
import br.edu.ifpb.es.bd2.model.Categoria;
import br.edu.ifpb.es.bd2.model.Produto;
import br.edu.ifpb.es.bd2.model.Role;
import br.edu.ifpb.es.bd2.model.Usuario;
import br.edu.ifpb.es.bd2.repository.CategoriaRepository;
import br.edu.ifpb.es.bd2.repository.ProdutoRepository;
import br.edu.ifpb.es.bd2.repository.UsuarioRepository;
import br.edu.ifpb.es.bd2.service.ProdutoService;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ProdutoService service;

    private final ProdutoRequestDTO request = new ProdutoRequestDTO(
            " Notebook ", "Descricao", new BigDecimal("10.00"), 5, "cat-1", "user-1");

    @Test
    @DisplayName("Deve preencher os nomes de categoria e vendedor na criação")
    void devePreencherDadosDesnormalizados() {
        when(categoriaRepository.findById("cat-1")).thenReturn(Optional.of(categoria()));
        when(usuarioRepository.findById("user-1")).thenReturn(Optional.of(usuario(Role.VENDEDOR, true)));
        when(repository.save(any(Produto.class))).thenAnswer(i -> i.getArgument(0));

        ProdutoResponseDTO criado = service.criar(request);

        assertEquals("Notebook", criado.nome());
        assertEquals("Informática", criado.categoriaNome());
        assertEquals("Vendedor João", criado.vendedorNome());
        assertEquals(0.0, criado.mediaAvaliacoes());
        assertEquals(0, criado.totalAvaliacoes());
    }

    @Test
    @DisplayName("Deve rejeitar categoria inexistente")
    void deveRejeitarCategoriaInexistente() {
        when(categoriaRepository.findById("cat-1")).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.criar(request));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar usuário que não é vendedor")
    void deveRejeitarUsuarioQueNaoEVendedor() {
        when(categoriaRepository.findById("cat-1")).thenReturn(Optional.of(categoria()));
        when(usuarioRepository.findById("user-1")).thenReturn(Optional.of(usuario(Role.CLIENTE, true)));

        assertThrows(VendedorInvalidoException.class, () -> service.criar(request));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar vendedor inativo")
    void deveRejeitarVendedorInativo() {
        when(categoriaRepository.findById("cat-1")).thenReturn(Optional.of(categoria()));
        when(usuarioRepository.findById("user-1")).thenReturn(Optional.of(usuario(Role.VENDEDOR, false)));

        assertThrows(VendedorInvalidoException.class, () -> service.criar(request));
    }

    @Test
    @DisplayName("Deve rejeitar decremento de quantidade não positiva")
    void deveRejeitarQuantidadeNaoPositiva() {
        assertThrows(IllegalArgumentException.class, () -> service.decrementarEstoque("p-1", 0));
    }

    @Test
    @DisplayName("Deve devolver o estoque com incremento atômico")
    void deveDevolverEstoque() {
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Produto.class)))
                .thenReturn(UpdateResult.acknowledged(1, 1L, null));

        service.devolverEstoque("p-1", 3);

        ArgumentCaptor<Update> captor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).updateFirst(any(Query.class), captor.capture(), eq(Produto.class));
        Document incremento = (Document) captor.getValue().getUpdateObject().get("$inc");
        assertEquals(3, incremento.get("estoque"));
    }

    @Test
    @DisplayName("Deve rejeitar devolução de estoque para produto inexistente")
    void deveRejeitarDevolucaoParaProdutoInexistente() {
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Produto.class)))
                .thenReturn(UpdateResult.acknowledged(0, 0L, null));

        assertThrows(RecursoNaoEncontradoException.class, () -> service.devolverEstoque("p-9", 1));
    }

    @Test
    @DisplayName("Deve rejeitar devolução de quantidade não positiva")
    void deveRejeitarDevolucaoNaoPositiva() {
        assertThrows(IllegalArgumentException.class, () -> service.devolverEstoque("p-1", 0));
    }

    @Test
    @DisplayName("Deve rejeitar métricas de avaliação negativas")
    void deveRejeitarMetricasNegativas() {
        assertThrows(IllegalArgumentException.class, () -> service.atualizarMetricasAvaliacao("p-1", 4.5, -1));
    }

    private Categoria categoria() {
        return Categoria.builder().id("cat-1").nome("Informática").build();
    }

    private Usuario usuario(Role role, boolean ativo) {
        Usuario usuario = new Usuario();
        usuario.setId("user-1");
        usuario.setNome("Vendedor João");
        usuario.setRole(role);
        usuario.setAtivo(ativo);
        return usuario;
    }
}
