package br.edu.ifpb.es.bd2.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final HttpStatus status;

    protected BusinessException(String mensagem, HttpStatus status) {
        super(mensagem);
        this.status = status;
    }
}