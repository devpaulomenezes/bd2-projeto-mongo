package br.edu.ifpb.es.bd2.exception;

import org.springframework.http.HttpStatus;

public class OperacaoNaoPermitidaException extends BusinessException {
    public OperacaoNaoPermitidaException(String mensagem) {
        super(mensagem, HttpStatus.FORBIDDEN);
    }
}
