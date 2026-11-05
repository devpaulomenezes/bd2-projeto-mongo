package br.edu.ifpb.es.bd2.exception;

import org.springframework.http.HttpStatus;

public class RequisicaoInvalidaException extends BusinessException {
    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem, HttpStatus.BAD_REQUEST);
    }
}
