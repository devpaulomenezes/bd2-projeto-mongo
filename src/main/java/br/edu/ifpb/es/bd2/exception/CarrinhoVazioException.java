package br.edu.ifpb.es.bd2.exception;

import org.springframework.http.HttpStatus;

public class CarrinhoVazioException extends BusinessException {
    public CarrinhoVazioException(String clienteId) {
        super("O carrinho do cliente está vazio: " + clienteId, HttpStatus.BAD_REQUEST);
    }
}
