package br.edu.ifpb.es.bd2.exception;

import org.springframework.http.HttpStatus;

public class AvaliacaoDuplicadaException extends BusinessException {
    public AvaliacaoDuplicadaException(String produtoId) {
        super("O cliente já avaliou este produto: " + produtoId, HttpStatus.CONFLICT);
    }
}
