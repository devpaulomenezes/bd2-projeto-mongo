package br.edu.ifpb.es.bd2.repository;

import br.edu.ifpb.es.bd2.dto.MetricasAvaliacaoDTO;
import br.edu.ifpb.es.bd2.model.Avaliacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvaliacaoRepository extends MongoRepository<Avaliacao, String> {

    Page<Avaliacao> findByProdutoId(String produtoId, Pageable pageable);

    boolean existsByClienteIdAndProdutoId(String clienteId, String produtoId);

    @Aggregation(pipeline = {
            "{ '$match': { 'produtoId': ?0 } }",
            "{ '$group': { '_id': null, 'media': { '$avg': '$nota' }, 'total': { '$sum': 1 } } }"
    })
    MetricasAvaliacaoDTO calcularMetricas(String produtoId);
}
