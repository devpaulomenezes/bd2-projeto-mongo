package br.edu.ifpb.es.bd2.exception;

import org.springframework.http.HttpStatus;

public class ClienteInvalidoException extends BusinessException {
    public ClienteInvalidoException(String id) {
        super("O usuário informado não é um cliente ativo: " + id, HttpStatus.BAD_REQUEST);
    }
}
