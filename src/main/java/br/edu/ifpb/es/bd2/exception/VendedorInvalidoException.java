package br.edu.ifpb.es.bd2.exception;

import org.springframework.http.HttpStatus;

public class VendedorInvalidoException extends BusinessException {
    public VendedorInvalidoException(String id) {
        super("O usuário informado não é um vendedor ativo: " + id, HttpStatus.BAD_REQUEST);
    }
}
