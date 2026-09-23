package br.edu.ifpb.es.bd2.exception;

import org.springframework.http.HttpStatus;

public class NomeDuplicadoException extends BusinessException {
    public NomeDuplicadoException(String nome) {
        super("Já existe um registro com o nome: " + nome, HttpStatus.CONFLICT);
    }
}