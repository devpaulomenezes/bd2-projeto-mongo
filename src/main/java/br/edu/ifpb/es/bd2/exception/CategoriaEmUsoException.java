package br.edu.ifpb.es.bd2.exception;

import org.springframework.http.HttpStatus;

public class CategoriaEmUsoException extends BusinessException {
    public CategoriaEmUsoException(String id) {
        super("A categoria possui produtos vinculados e não pode ser removida: " + id, HttpStatus.CONFLICT);
    }
}
