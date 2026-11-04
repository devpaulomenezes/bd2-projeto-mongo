package br.edu.ifpb.es.bd2.controller;

import br.edu.ifpb.es.bd2.dto.ProdutoRequestDTO;
import br.edu.ifpb.es.bd2.dto.ProdutoResponseDTO;
import br.edu.ifpb.es.bd2.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Catálogo e busca de produtos da vitrine")
public class ProdutoController {

    private final ProdutoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um produto, preenchendo os nomes de categoria e vendedor")
    public ProdutoResponseDTO criar(@Valid @RequestBody ProdutoRequestDTO req) {
        return service.criar(req);
    }

    @GetMapping
    @Operation(summary = "Busca produtos, combinando os filtros informados")
    public List<ProdutoResponseDTO> buscar(
            @Parameter(description = "Trecho do nome, sem diferenciar maiúsculas") @RequestParam(required = false) String nome,
            @Parameter(description = "Id da categoria") @RequestParam(required = false) String categoriaId,
            @Parameter(description = "Preço mínimo, inclusive") @RequestParam(required = false) BigDecimal precoMin,
            @Parameter(description = "Preço máximo, inclusive") @RequestParam(required = false) BigDecimal precoMax) {
        return service.buscar(nome, categoriaId, precoMin, precoMax);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um produto pelo id")
    public ProdutoResponseDTO buscarPorId(@PathVariable String id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um produto, preservando as métricas de avaliação")
    public ProdutoResponseDTO atualizar(@PathVariable String id, @Valid @RequestBody ProdutoRequestDTO req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um produto")
    public void deletar(@PathVariable String id) {
        service.deletar(id);
    }
}
