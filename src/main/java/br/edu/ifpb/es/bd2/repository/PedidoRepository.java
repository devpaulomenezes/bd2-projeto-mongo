package br.edu.ifpb.es.bd2.repository;

import br.edu.ifpb.es.bd2.model.Pedido;
import br.edu.ifpb.es.bd2.model.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends MongoRepository<Pedido, String> {

    Page<Pedido> findByClienteId(String clienteId, Pageable pageable);

    List<Pedido> findByStatus(StatusPedido status);
}
