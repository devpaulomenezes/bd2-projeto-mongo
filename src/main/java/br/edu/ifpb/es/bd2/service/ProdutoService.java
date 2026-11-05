package br.edu.ifpb.es.bd2.service;

import br.edu.ifpb.es.bd2.dto.ProdutoRequestDTO;
import br.edu.ifpb.es.bd2.dto.ProdutoResponseDTO;
import br.edu.ifpb.es.bd2.exception.EstoqueInsuficienteException;
import br.edu.ifpb.es.bd2.exception.RecursoNaoEncontradoException;
import br.edu.ifpb.es.bd2.exception.VendedorInvalidoException;
import br.edu.ifpb.es.bd2.model.Categoria;
import br.edu.ifpb.es.bd2.model.Produto;
import br.edu.ifpb.es.bd2.model.Role;
import br.edu.ifpb.es.bd2.model.Usuario;
import br.edu.ifpb.es.bd2.repository.CategoriaRepository;
import br.edu.ifpb.es.bd2.repository.ProdutoRepository;
import br.edu.ifpb.es.bd2.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.Decimal128;
import org.springframework.data.domain.Range;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MongoTemplate mongoTemplate;

    public ProdutoResponseDTO criar(ProdutoRequestDTO req) {
        Categoria categoria = buscarCategoria(req.categoriaId());
        Usuario vendedor = buscarVendedor(req.vendedorId());

        Produto produto = Produto.builder()
                .nome(req.nome().trim())
                .descricao(req.descricao())
                .preco(req.preco())
                .estoque(req.estoque())
                .categoriaId(categoria.getId())
                .categoriaNome(categoria.getNome())
                .vendedorId(vendedor.getId())
                .vendedorNome(vendedor.getNome())
                .build();
        return ProdutoResponseDTO.from(repository.save(produto));
    }

    public List<ProdutoResponseDTO> buscar(String nome, String categoriaId, BigDecimal precoMin, BigDecimal precoMax) {
        String termo = nome != null ? nome.trim() : "";
        // Limite inferior sempre presente: uma faixa sem nenhum limite vira {preco: {}} e nao retorna nada.
        // Limites em Decimal128: com dois limites o Spring Data enviaria BigDecimal como String, sem casar com o campo.
        Range.Bound<Decimal128> minimo = Range.Bound.inclusive(new Decimal128(precoMin != null ? precoMin : BigDecimal.ZERO));
        Range.Bound<Decimal128> maximo = precoMax != null ? Range.Bound.inclusive(new Decimal128(precoMax)) : Range.Bound.unbounded();
        Range<Decimal128> faixaPreco = Range.of(minimo, maximo);

        List<Produto> produtos = StringUtils.hasText(categoriaId)
                ? repository.findByNomeContainingIgnoreCaseAndCategoriaIdAndPrecoBetween(termo, categoriaId, faixaPreco)
                : repository.findByNomeContainingIgnoreCaseAndPrecoBetween(termo, faixaPreco);
        return produtos.stream().map(ProdutoResponseDTO::from).toList();
    }

    public ProdutoResponseDTO buscarPorId(String id) {
        return ProdutoResponseDTO.from(buscarEntidade(id));
    }

    public ProdutoResponseDTO atualizar(String id, ProdutoRequestDTO req) {
        buscarEntidade(id);
        Categoria categoria = buscarCategoria(req.categoriaId());
        Usuario vendedor = buscarVendedor(req.vendedorId());

        // $set apenas nos campos editaveis, para nao sobrescrever as metricas de avaliacao
        Update update = new Update()
                .set("nome", req.nome().trim())
                .set("descricao", req.descricao())
                .set("preco", req.preco())
                .set("estoque", req.estoque())
                .set("categoriaId", categoria.getId())
                .set("categoriaNome", categoria.getNome())
                .set("vendedorId", vendedor.getId())
                .set("vendedorNome", vendedor.getNome());
        Produto atualizado = mongoTemplate.findAndModify(porId(id), update,
                FindAndModifyOptions.options().returnNew(true), Produto.class);
        if (atualizado == null) {
            throw new RecursoNaoEncontradoException("Produto", id);
        }
        return ProdutoResponseDTO.from(atualizado);
    }

    public void deletar(String id) {
        repository.delete(buscarEntidade(id));
    }

    // Filtro e $inc na mesma operacao: compras concorrentes nunca deixam o estoque negativo
    public void decrementarEstoque(String id, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero");
        }
        Query comEstoque = porId(id).addCriteria(Criteria.where("estoque").gte(quantidade));
        Produto atualizado = mongoTemplate.findAndModify(comEstoque, new Update().inc("estoque", -quantidade), Produto.class);
        if (atualizado == null) {
            buscarEntidade(id);
            throw new EstoqueInsuficienteException(id, quantidade);
        }
    }

    public void devolverEstoque(String id, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero");
        }
        if (mongoTemplate.updateFirst(porId(id), new Update().inc("estoque", quantidade), Produto.class).getMatchedCount() == 0) {
            throw new RecursoNaoEncontradoException("Produto", id);
        }
    }

    // Recebe os valores ja agregados a partir da colecao de avaliacoes, cobrindo criacao, edicao e remocao
    public void atualizarMetricasAvaliacao(String id, double mediaAvaliacoes, int totalAvaliacoes) {
        if (totalAvaliacoes < 0 || mediaAvaliacoes < 0) {
            throw new IllegalArgumentException("As métricas de avaliação não podem ser negativas");
        }
        Update update = new Update()
                .set("mediaAvaliacoes", mediaAvaliacoes)
                .set("totalAvaliacoes", totalAvaliacoes);
        if (mongoTemplate.updateFirst(porId(id), update, Produto.class).getMatchedCount() == 0) {
            throw new RecursoNaoEncontradoException("Produto", id);
        }
    }

    private Produto buscarEntidade(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
    }

    private Categoria buscarCategoria(String id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }

    private Usuario buscarVendedor(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vendedor", id));
        if (usuario.getRole() != Role.VENDEDOR || !Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new VendedorInvalidoException(id);
        }
        return usuario;
    }

    private Query porId(String id) {
        return Query.query(Criteria.where("id").is(id));
    }
}
