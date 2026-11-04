package br.edu.ifpb.es.bd2.exception;

import org.springframework.http.HttpStatus;

public class EstoqueInsuficienteException extends BusinessException {
    public EstoqueInsuficienteException(String produtoId, int quantidade) {
        super("Estoque insuficiente para retirar " + quantidade + " unidade(s) do produto: " + produtoId, HttpStatus.CONFLICT);
    }
}
