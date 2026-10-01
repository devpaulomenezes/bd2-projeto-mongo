package br.edu.ifpb.es.bd2.service;

import br.edu.ifpb.es.bd2.dto.*;
import br.edu.ifpb.es.bd2.model.Cupom;
import br.edu.ifpb.es.bd2.model.StatusCupom;
import br.edu.ifpb.es.bd2.repository.CupomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CupomService {

    private final CupomRepository repository;

    public CupomService(CupomRepository repository) {
        this.repository = repository;
    }

    public CupomResponse criar(CupomRequest req) {
        String codigo = req.codigo().trim().toUpperCase();
        if (repository.existsByCodigo(codigo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um cupom com esse código");
        }
        Cupom cupom = new Cupom();
        preencher(cupom, req, codigo);
        return CupomResponse.from(repository.save(cupom));
    }

    public List<CupomResponse> listar() {
        return repository.findAll().stream().map(CupomResponse::from).toList();
    }

    public CupomResponse buscarPorId(String id) {
        return CupomResponse.from(obterPorId(id));
    }

    public CupomResponse atualizar(String id, CupomRequest req) {
        Cupom cupom = obterPorId(id);
        String codigo = req.codigo().trim().toUpperCase();
        if (!cupom.getCodigo().equals(codigo) && repository.existsByCodigo(codigo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um cupom com esse código");
        }
        preencher(cupom, req, codigo);
        return CupomResponse.from(repository.save(cupom));
    }

    public void deletar(String id) {
        repository.delete(obterPorId(id));
    }

    public CupomValidacaoResponse validar(String codigo) {
        Cupom cupom = repository.findByCodigo(codigo.trim().toUpperCase()).orElse(null);

        if (cupom == null) {
            return new CupomValidacaoResponse(false, "Cupom não encontrado", null);
        }
        if (cupom.getStatus() != StatusCupom.ATIVO) {
            return new CupomValidacaoResponse(false, "Cupom não está ativo", null);
        }
        if (cupom.getDataExpiracao().isBefore(LocalDateTime.now())) {
            cupom.setStatus(StatusCupom.EXPIRADO);
            repository.save(cupom);
            return new CupomValidacaoResponse(false, "Cupom expirado", null);
        }
        return new CupomValidacaoResponse(true, "Cupom válido", cupom.getPercentualDesconto());
    }

    private Cupom obterPorId(String id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Cupom não encontrado"));
    }

    private void preencher(Cupom cupom, CupomRequest req, String codigo) {
        cupom.setCodigo(codigo);
        cupom.setPercentualDesconto(req.percentualDesconto());
        cupom.setDataExpiracao(req.dataExpiracao());
        cupom.setStatus(req.status() != null ? req.status() : StatusCupom.ATIVO);
    }
}