package br.edu.ifpb.es.bd2.controller;

import br.edu.ifpb.es.bd2.dto.LimpezaCarrinhoResponseDTO;
import br.edu.ifpb.es.bd2.service.CarrinhoLimpezaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/carrinhos/limpeza")
@RequiredArgsConstructor
@Tag(name = "Carrinho", description = "Itens do carrinho de compras de um cliente")
public class CarrinhoLimpezaController {

    private final CarrinhoLimpezaService service;

    @PostMapping
    @Operation(summary = "Executa a rotina de limpeza: remove carrinhos inativos, itens de produtos excluídos e carrinhos vazios")
    public LimpezaCarrinhoResponseDTO executar() {
        return service.executar();
    }
}
