package br.edu.ifpb.es.bd2.repository;

import br.edu.ifpb.es.bd2.model.Cupom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CupomRepository extends MongoRepository<Cupom, String> {
    Optional<Cupom> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}