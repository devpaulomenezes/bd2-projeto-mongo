package br.edu.ifpb.es.bd2.controller;

import br.edu.ifpb.es.bd2.dto.AvaliacaoRequestDTO;
import br.edu.ifpb.es.bd2.dto.AvaliacaoResponseDTO;
import br.edu.ifpb.es.bd2.dto.PaginaResponseDTO;
import br.edu.ifpb.es.bd2.service.AvaliacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Avaliações", description = "Avaliações de produtos, consultadas em páginas")
public class AvaliacaoController {

    private final AvaliacaoService service;

    @PostMapping("/produtos/{produtoId}/avaliacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra a avaliação de um cliente e recalcula a média do produto")
    public AvaliacaoResponseDTO criar(@PathVariable String produtoId, @Valid @RequestBody AvaliacaoRequestDTO req) {
        return service.criar(produtoId, req);
    }

    @GetMapping("/produtos/{produtoId}/avaliacoes")
    @Operation(summary = "Lista as avaliações de um produto em páginas, das mais recentes para as mais antigas")
    public PaginaResponseDTO<AvaliacaoResponseDTO> listar(
            @PathVariable String produtoId,
            @Parameter(description = "Número da página, começando em 0")
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "A página não pode ser negativa") int pagina,
            @Parameter(description = "Quantidade de avaliações por página (máximo 50)")
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "O tamanho da página deve ser no mínimo 1")
            @Max(value = 50, message = "O tamanho da página deve ser no máximo 50") int tamanho) {
        return service.listarPorProduto(produtoId, pagina, tamanho);
    }

    @GetMapping("/avaliacoes/{id}")
    @Operation(summary = "Busca uma avaliação pelo id")
    public AvaliacaoResponseDTO buscarPorId(@PathVariable String id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/avaliacoes/{id}")
    @Operation(summary = "Altera a nota e o comentário de uma avaliação; apenas o autor pode alterar")
    public AvaliacaoResponseDTO atualizar(@PathVariable String id, @Valid @RequestBody AvaliacaoRequestDTO req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/avaliacoes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove uma avaliação e recalcula a média do produto")
    public void deletar(@PathVariable String id) {
        service.deletar(id);
    }
}
