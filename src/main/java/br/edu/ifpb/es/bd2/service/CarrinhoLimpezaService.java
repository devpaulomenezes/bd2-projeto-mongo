package br.edu.ifpb.es.bd2.service;

import br.edu.ifpb.es.bd2.dto.LimpezaCarrinhoResponseDTO;
import br.edu.ifpb.es.bd2.model.Carrinho;
import br.edu.ifpb.es.bd2.model.Produto;
import br.edu.ifpb.es.bd2.repository.CarrinhoRepository;
import br.edu.ifpb.es.bd2.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarrinhoLimpezaService {

    private final CarrinhoRepository carrinhoRepository;
    private final ProdutoRepository produtoRepository;

    @Value("${carrinho.limpeza.dias-inatividade:7}")
    private int diasInatividade = 7;

    public LimpezaCarrinhoResponseDTO executar() {
        return executar(diasInatividade);
    }

    public LimpezaCarrinhoResponseDTO executar(int diasInatividade) {
        LocalDateTime agora = LocalDateTime.now();

        long inativos = carrinhoRepository.deleteByAtualizadoEmBefore(agora.minusDays(diasInatividade));

        List<Carrinho> restantes = carrinhoRepository.findAll();
        Set<String> produtosExistentes = buscarProdutosExistentes(restantes);

        int itensRemovidos = 0;
        long vaziosRemovidos = 0;
        for (Carrinho carrinho : restantes) {
            int antes = carrinho.getItens().size();
            carrinho.getItens().removeIf(i -> !produtosExistentes.contains(i.getProdutoId()));
            int removidos = antes - carrinho.getItens().size();
            itensRemovidos += removidos;

            if (carrinho.getItens().isEmpty()) {
                carrinhoRepository.delete(carrinho);
                vaziosRemovidos++;
            } else if (removidos > 0) {
                carrinhoRepository.save(carrinho);
            }
        }
        return new LimpezaCarrinhoResponseDTO(inativos, itensRemovidos, vaziosRemovidos, agora);
    }

    private Set<String> buscarProdutosExistentes(List<Carrinho> carrinhos) {
        Set<String> ids = carrinhos.stream()
                .flatMap(c -> c.getItens().stream())
                .map(i -> i.getProdutoId())
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return new HashSet<>();
        }
        return produtoRepository.findAllById(ids).stream()
                .map(Produto::getId)
                .collect(Collectors.toSet());
    }
}
