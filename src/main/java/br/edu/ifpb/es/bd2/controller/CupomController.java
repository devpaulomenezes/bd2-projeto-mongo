package br.edu.ifpb.es.bd2.controller;

import br.edu.ifpb.es.bd2.dto.*;
import br.edu.ifpb.es.bd2.service.CupomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cupons")
public class CupomController {

    private final CupomService service;

    public CupomController(CupomService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CupomResponse> criar(@RequestBody @Valid CupomRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
    }

    @GetMapping
    public List<CupomResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public CupomResponse buscarPorId(@PathVariable String id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public CupomResponse atualizar(@PathVariable String id, @RequestBody @Valid CupomRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validar/{codigo}")
    public CupomValidacaoResponse validar(@PathVariable String codigo) {
        return service.validar(codigo);
    }
}