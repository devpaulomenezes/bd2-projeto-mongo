package br.edu.ifpb.es.bd2.controller;

import br.edu.ifpb.es.bd2.dto.CheckoutRequestDTO;
import br.edu.ifpb.es.bd2.dto.PaginaResponseDTO;
import br.edu.ifpb.es.bd2.dto.PedidoResponseDTO;
import br.edu.ifpb.es.bd2.service.PedidoService;
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
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Checkout do carrinho e consulta de pedidos")
public class PedidoController {

    private final PedidoService service;

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Finaliza a compra do carrinho: valida o cupom, debita o estoque e congela os dados do pedido")
    public PedidoResponseDTO checkout(@Valid @RequestBody CheckoutRequestDTO req) {
        return service.checkout(req);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um pedido pelo id")
    public PedidoResponseDTO buscarPorId(@PathVariable String id) {
        return service.buscarPorId(id);
    }

    @GetMapping
    @Operation(summary = "Lista os pedidos de um cliente em páginas, dos mais recentes para os mais antigos")
    public PaginaResponseDTO<PedidoResponseDTO> listarPorCliente(
            @Parameter(description = "Id do cliente") @RequestParam String clienteId,
            @Parameter(description = "Número da página, começando em 0")
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "A página não pode ser negativa") int pagina,
            @Parameter(description = "Quantidade de pedidos por página (máximo 50)")
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "O tamanho da página deve ser no mínimo 1")
            @Max(value = 50, message = "O tamanho da página deve ser no máximo 50") int tamanho) {
        return service.listarPorCliente(clienteId, pagina, tamanho);
    }
}
