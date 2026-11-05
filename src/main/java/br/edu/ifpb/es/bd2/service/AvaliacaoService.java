package br.edu.ifpb.es.bd2.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepository repository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoService produtoService;

    public AvaliacaoResponseDTO criar(String produtoId, AvaliacaoRequestDTO req) {
        garantirProduto(produtoId);
        Usuario cliente = validarCliente(req.clienteId());
        if (repository.existsByClienteIdAndProdutoId(cliente.getId(), produtoId)) {
            throw new AvaliacaoDuplicadaException(produtoId);
        }

        LocalDateTime agora = LocalDateTime.now();
        Avaliacao avaliacao = Avaliacao.builder()
                .produtoId(produtoId)
                .clienteId(cliente.getId())
                .clienteNome(cliente.getNome())
                .nota(req.nota())
                .comentario(req.comentario())
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build();

        Avaliacao salva;
        try {
            salva = repository.save(avaliacao);
        } catch (DuplicateKeyException e) {
            throw new AvaliacaoDuplicadaException(produtoId);
        }
        recalcularMetricas(produtoId);
        return AvaliacaoResponseDTO.from(salva);
    }

    public PaginaResponseDTO<AvaliacaoResponseDTO> listarPorProduto(String produtoId, int pagina, int tamanho) {
        garantirProduto(produtoId);
        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "criadoEm"));
        return PaginaResponseDTO.from(repository.findByProdutoId(produtoId, pageable).map(AvaliacaoResponseDTO::from));
    }

    public AvaliacaoResponseDTO buscarPorId(String id) {
        return AvaliacaoResponseDTO.from(buscarEntidade(id));
    }

    public AvaliacaoResponseDTO atualizar(String id, AvaliacaoRequestDTO req) {
        Avaliacao avaliacao = buscarEntidade(id);
        if (!avaliacao.getClienteId().equals(req.clienteId())) {
            throw new OperacaoNaoPermitidaException("Apenas o cliente que fez a avaliação pode alterá-la");
        }

        avaliacao.setNota(req.nota());
        avaliacao.setComentario(req.comentario());
        avaliacao.setAtualizadoEm(LocalDateTime.now());
        Avaliacao salva = repository.save(avaliacao);
        recalcularMetricas(salva.getProdutoId());
        return AvaliacaoResponseDTO.from(salva);
    }

    public void deletar(String id) {
        Avaliacao avaliacao = buscarEntidade(id);
        repository.delete(avaliacao);
        recalcularMetricas(avaliacao.getProdutoId());
    }

    private void recalcularMetricas(String produtoId) {
        if (!produtoRepository.existsById(produtoId)) {
            return;
        }
        MetricasAvaliacaoDTO metricas = repository.calcularMetricas(produtoId);
        double media = metricas != null && metricas.media() != null ? arredondar(metricas.media()) : 0.0;
        int total = metricas != null && metricas.total() != null ? metricas.total() : 0;
        produtoService.atualizarMetricasAvaliacao(produtoId, media, total);
    }

    private double arredondar(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private void garantirProduto(String produtoId) {
        if (!produtoRepository.existsById(produtoId)) {
            throw new RecursoNaoEncontradoException("Produto", produtoId);
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

    private Avaliacao buscarEntidade(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliação", id));
    }
}
