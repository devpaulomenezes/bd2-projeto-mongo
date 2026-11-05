package br.edu.ifpb.es.bd2.exception;

import org.springframework.http.HttpStatus;

public class CupomInvalidoException extends BusinessException {
    public CupomInvalidoException(String mensagem) {
        super(mensagem, HttpStatus.BAD_REQUEST);
    }
}
