package br.edu.ifpb.es.bd2.repository;

import br.edu.ifpb.es.bd2.model.Carrinho;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CarrinhoRepository extends MongoRepository<Carrinho, String> {

    Optional<Carrinho> findByClienteId(String clienteId);

    boolean existsByClienteId(String clienteId);

    void deleteByClienteId(String clienteId);
}