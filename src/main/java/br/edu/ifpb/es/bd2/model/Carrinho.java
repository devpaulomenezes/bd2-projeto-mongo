package br.edu.ifpb.es.bd2.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "carrinhos")
public class Carrinho {

    @Id
    private String id;

    @Indexed(unique = true)
    private String clienteId;

    private List<ItemCarrinhoEmbedded> itens = new ArrayList<>();

    @Indexed
    private LocalDateTime atualizadoEm = LocalDateTime.now();

    public Carrinho() {}

    public Carrinho(String clienteId) {
        this.clienteId = clienteId;
    }

    public BigDecimal getValorTotal() {
        return itens.stream()
                .map(ItemCarrinhoEmbedded::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void registrarAtividade() {
        this.atualizadoEm = LocalDateTime.now();
    }

    // getters e setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public List<ItemCarrinhoEmbedded> getItens() { return itens; }
    public void setItens(List<ItemCarrinhoEmbedded> itens) { this.itens = itens; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
