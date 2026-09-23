package br.edu.ifpb.es.bd2.exception;

public class NomeDuplicadoException extends RuntimeException {
    public NomeDuplicadoException(String nome) {
        super("Já existe uma categoria com o nome: " + nome);
    }
}