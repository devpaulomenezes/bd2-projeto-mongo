package br.edu.ifpb.es.bd2.repository;

import br.edu.ifpb.es.bd2.model.Produto;
import org.bson.types.Decimal128;
import org.springframework.data.domain.Range;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends MongoRepository<Produto, String> {

    List<Produto> findByNomeContainingIgnoreCaseAndPrecoBetween(String nome, Range<Decimal128> preco);

    List<Produto> findByNomeContainingIgnoreCaseAndCategoriaIdAndPrecoBetween(String nome, String categoriaId, Range<Decimal128> preco);

    boolean existsByCategoriaId(String categoriaId);

    @Query("{ 'categoriaId' : ?0 }")
    @Update("{ '$set' : { 'categoriaNome' : ?1 } }")
    void atualizarCategoriaNome(String categoriaId, String categoriaNome);
}
