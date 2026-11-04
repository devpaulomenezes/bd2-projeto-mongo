package br.edu.ifpb.es.bd2.model;

import java.math.BigDecimal;

public class ItemCarrinhoEmbedded {

    private String produtoId;
    private String nome;
    private BigDecimal precoUnitario;
    private Integer quantidade;

    public ItemCarrinhoEmbedded() {}

    public ItemCarrinhoEmbedded(String produtoId, String nome, BigDecimal precoUnitario, Integer quantidade) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.precoUnitario = precoUnitario;
        this.quantidade = quantidade;
    }

    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    // getters e setters
    public String getProdutoId() { return produtoId; }
    public void setProdutoId(String produtoId) { this.produtoId = produtoId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
}