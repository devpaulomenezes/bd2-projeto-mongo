package br.edu.ifpb.es.bd2;

import br.edu.ifpb.es.bd2.dto.AvaliacaoRequestDTO;
import br.edu.ifpb.es.bd2.dto.AvaliacaoResponseDTO;
import br.edu.ifpb.es.bd2.dto.MetricasAvaliacaoDTO;
import br.edu.ifpb.es.bd2.dto.PaginaResponseDTO;
import br.edu.ifpb.es.bd2.exception.AvaliacaoDuplicadaException;
import br.edu.ifpb.es.bd2.exception.ClienteInvalidoException;
import br.edu.ifpb.es.bd2.exception.OperacaoNaoPermitidaException;
import br.edu.ifpb.es.bd2.exception.RecursoNaoEncontradoException;
import br.edu.ifpb.es.bd2.model.Avaliacao;
import br.edu.ifpb.es.bd2.model.Role;
import br.edu.ifpb.es.bd2.model.Usuario;
import br.edu.ifpb.es.bd2.repository.AvaliacaoRepository;
import br.edu.ifpb.es.bd2.repository.ProdutoRepository;
import br.edu.ifpb.es.bd2.repository.UsuarioRepository;
import br.edu.ifpb.es.bd2.service.AvaliacaoService;
import br.edu.ifpb.es.bd2.service.ProdutoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock
    private AvaliacaoRepository repository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private AvaliacaoService service;

    @Test
    @DisplayName("Deve registrar a avaliação e gravar no produto a média arredondada e o total")
    void deveCriarAvaliacaoERecalcularMetricas() {
        when(produtoRepository.existsById("p-1")).thenReturn(true);
        when(usuarioRepository.findById("c-1")).thenReturn(Optional.of(usuario(Role.CLIENTE, true)));
        when(repository.save(any(Avaliacao.class))).thenAnswer(i -> i.getArgument(0));
        when(repository.calcularMetricas("p-1")).thenReturn(new MetricasAvaliacaoDTO(4.666666, 3));

        AvaliacaoResponseDTO criada = service.criar("p-1", new AvaliacaoRequestDTO("c-1", 5, "Muito bom"));

        assertEquals("Cliente Maria", criada.clienteNome());
        assertEquals(5, criada.nota());
        assertEquals("p-1", criada.produtoId());
        verify(produtoService).atualizarMetricasAvaliacao("p-1", 4.67, 3);
    }

    @Test
    @DisplayName("Deve rejeitar a segunda avaliação do mesmo cliente para o mesmo produto")
    void deveRejeitarAvaliacaoDuplicada() {
        when(produtoRepository.existsById("p-1")).thenReturn(true);
        when(usuarioRepository.findById("c-1")).thenReturn(Optional.of(usuario(Role.CLIENTE, true)));
        when(repository.existsByClienteIdAndProdutoId("c-1", "p-1")).thenReturn(true);

        assertThrows(AvaliacaoDuplicadaException.class,
                () -> service.criar("p-1", new AvaliacaoRequestDTO("c-1", 4, null)));
        verify(repository, never()).save(any());
        verify(produtoService, never()).atualizarMetricasAvaliacao(any(), anyDouble(), anyInt());
    }

    @Test
    @DisplayName("Deve converter a violação do índice único em avaliação duplicada")
    void deveTratarViolacaoDoIndiceUnico() {
        when(produtoRepository.existsById("p-1")).thenReturn(true);
        when(usuarioRepository.findById("c-1")).thenReturn(Optional.of(usuario(Role.CLIENTE, true)));
        when(repository.save(any(Avaliacao.class))).thenThrow(new DuplicateKeyException("E11000 duplicate key error"));

        assertThrows(AvaliacaoDuplicadaException.class,
                () -> service.criar("p-1", new AvaliacaoRequestDTO("c-1", 4, null)));
        verify(produtoService, never()).atualizarMetricasAvaliacao(any(), anyDouble(), anyInt());
    }

    @Test
    @DisplayName("Deve rejeitar avaliação de usuário que não é cliente")
    void deveRejeitarUsuarioQueNaoECliente() {
        when(produtoRepository.existsById("p-1")).thenReturn(true);
        when(usuarioRepository.findById("c-1")).thenReturn(Optional.of(usuario(Role.VENDEDOR, true)));

        assertThrows(ClienteInvalidoException.class,
                () -> service.criar("p-1", new AvaliacaoRequestDTO("c-1", 5, null)));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar avaliação de produto inexistente")
    void deveRejeitarProdutoInexistente() {
        when(produtoRepository.existsById("p-1")).thenReturn(false);

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.criar("p-1", new AvaliacaoRequestDTO("c-1", 5, null)));
        verify(usuarioRepository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve permitir que apenas o autor altere a avaliação")
    void deveRejeitarAlteracaoPorOutroCliente() {
        when(repository.findById("a-1")).thenReturn(Optional.of(avaliacao()));

        assertThrows(OperacaoNaoPermitidaException.class,
                () -> service.atualizar("a-1", new AvaliacaoRequestDTO("c-2", 1, "Não gostei")));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve alterar a nota e recalcular a média do produto")
    void deveAlterarAvaliacaoERecalcularMetricas() {
        when(repository.findById("a-1")).thenReturn(Optional.of(avaliacao()));
        when(repository.save(any(Avaliacao.class))).thenAnswer(i -> i.getArgument(0));
        when(produtoRepository.existsById("p-1")).thenReturn(true);
        when(repository.calcularMetricas("p-1")).thenReturn(new MetricasAvaliacaoDTO(2.0, 1));

        AvaliacaoResponseDTO alterada = service.atualizar("a-1", new AvaliacaoRequestDTO("c-1", 2, "Mudei de ideia"));

        assertEquals(2, alterada.nota());
        assertEquals("Mudei de ideia", alterada.comentario());
        verify(produtoService).atualizarMetricasAvaliacao("p-1", 2.0, 1);
    }

    @Test
    @DisplayName("Deve zerar as métricas do produto ao remover a última avaliação")
    void deveZerarMetricasAoRemoverUltimaAvaliacao() {
        Avaliacao avaliacao = avaliacao();
        when(repository.findById("a-1")).thenReturn(Optional.of(avaliacao));
        when(produtoRepository.existsById("p-1")).thenReturn(true);
        when(repository.calcularMetricas("p-1")).thenReturn(null);

        service.deletar("a-1");

        verify(repository).delete(avaliacao);
        verify(produtoService).atualizarMetricasAvaliacao("p-1", 0.0, 0);
    }

    @Test
    @DisplayName("Deve remover a avaliação de um produto já excluído sem recalcular métricas")
    void deveRemoverAvaliacaoDeProdutoExcluido() {
        Avaliacao avaliacao = avaliacao();
        when(repository.findById("a-1")).thenReturn(Optional.of(avaliacao));
        when(produtoRepository.existsById("p-1")).thenReturn(false);

        service.deletar("a-1");

        verify(repository).delete(avaliacao);
        verify(produtoService, never()).atualizarMetricasAvaliacao(any(), anyDouble(), anyInt());
    }

    @Test
    @DisplayName("Deve listar as avaliações em páginas ordenadas da mais recente para a mais antiga")
    void deveListarAvaliacoesPaginadas() {
        when(produtoRepository.existsById("p-1")).thenReturn(true);
        PageRequest segundaPagina = PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "criadoEm"));
        when(repository.findByProdutoId(eq("p-1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(avaliacao()), segundaPagina, 3));

        PaginaResponseDTO<AvaliacaoResponseDTO> pagina = service.listarPorProduto("p-1", 1, 2);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findByProdutoId(eq("p-1"), captor.capture());
        Pageable usado = captor.getValue();
        assertEquals(1, usado.getPageNumber());
        assertEquals(2, usado.getPageSize());
        assertEquals(Sort.Direction.DESC, usado.getSort().getOrderFor("criadoEm").getDirection());

        assertEquals(1, pagina.conteudo().size());
        assertEquals(1, pagina.pagina());
        assertEquals(2, pagina.tamanho());
        assertEquals(3, pagina.totalElementos());
        assertEquals(2, pagina.totalPaginas());
        assertTrue(pagina.ultima());
    }

    @Test
    @DisplayName("Deve responder não encontrado para avaliação inexistente")
    void deveRejeitarAvaliacaoInexistente() {
        when(repository.findById("a-9")).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorId("a-9"));
    }

    private Usuario usuario(Role role, boolean ativo) {
        Usuario usuario = new Usuario();
        usuario.setId("c-1");
        usuario.setNome("Cliente Maria");
        usuario.setRole(role);
        usuario.setAtivo(ativo);
        return usuario;
    }

    private Avaliacao avaliacao() {
        LocalDateTime criadaEm = LocalDateTime.now().minusDays(2);
        return Avaliacao.builder()
                .id("a-1")
                .produtoId("p-1")
                .clienteId("c-1")
                .clienteNome("Cliente Maria")
                .nota(5)
                .comentario("Muito bom")
                .criadoEm(criadaEm)
                .atualizadoEm(criadaEm)
                .build();
    }
}
