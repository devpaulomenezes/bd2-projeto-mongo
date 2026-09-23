package br.edu.ifpb.es.bd2.exception;

import org.springframework.http.HttpStatus;

public class RecursoNaoEncontradoException extends BusinessException {
    public RecursoNaoEncontradoException(String recurso, String id) {
        super(recurso + " não encontrado(a): " + id, HttpStatus.NOT_FOUND);
    }
}