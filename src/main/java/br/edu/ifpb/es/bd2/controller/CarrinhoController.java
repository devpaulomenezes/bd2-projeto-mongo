package br.edu.ifpb.es.bd2.controller;

import br.edu.ifpb.es.bd2.dto.CarrinhoResponseDTO;
import br.edu.ifpb.es.bd2.dto.ItemCarrinhoRequestDTO;
import br.edu.ifpb.es.bd2.dto.QuantidadeItemRequestDTO;
import br.edu.ifpb.es.bd2.service.CarrinhoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carrinhos/{clienteId}")
@RequiredArgsConstructor
@Tag(name = "Carrinho", description = "Itens do carrinho de compras de um cliente")
public class CarrinhoController {

    private final CarrinhoService service;

    @GetMapping
    @Operation(summary = "Consulta o carrinho do cliente; sem itens, retorna um carrinho vazio")
    public CarrinhoResponseDTO buscar(@PathVariable String clienteId) {
        return service.buscar(clienteId);
    }

    @PostMapping("/itens")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adiciona um produto ao carrinho, somando a quantidade se ele já estiver nele")
    public CarrinhoResponseDTO adicionarItem(@PathVariable String clienteId,
                                             @Valid @RequestBody ItemCarrinhoRequestDTO req) {
        return service.adicionarItem(clienteId, req);
    }

    @PutMapping("/itens/{produtoId}")
    @Operation(summary = "Altera a quantidade de um item que já está no carrinho")
    public CarrinhoResponseDTO alterarQuantidade(@PathVariable String clienteId,
                                                 @PathVariable String produtoId,
                                                 @Valid @RequestBody QuantidadeItemRequestDTO req) {
        return service.alterarQuantidade(clienteId, produtoId, req.quantidade());
    }

    @DeleteMapping("/itens/{produtoId}")
    @Operation(summary = "Remove um item do carrinho e retorna o carrinho atualizado")
    public CarrinhoResponseDTO removerItem(@PathVariable String clienteId, @PathVariable String produtoId) {
        return service.removerItem(clienteId, produtoId);
    }

    @DeleteMapping
    @Operation(summary = "Esvazia o carrinho do cliente e retorna um carrinho vazio")
    public CarrinhoResponseDTO limpar(@PathVariable String clienteId) {
        return service.limpar(clienteId);
    }
}
