package br.edu.ifpb.es.bd2.scheduler;

import br.edu.ifpb.es.bd2.dto.LimpezaCarrinhoResponseDTO;
import br.edu.ifpb.es.bd2.service.CarrinhoLimpezaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Dispara a limpeza automaticamente (por padrao, todo dia as 03:00). Desligue com carrinho.limpeza.habilitada=false
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "carrinho.limpeza.habilitada", havingValue = "true", matchIfMissing = true)
public class CarrinhoLimpezaScheduler {

    private final CarrinhoLimpezaService service;

    @Scheduled(cron = "${carrinho.limpeza.cron:0 0 3 * * *}")
    public void limparCarrinhos() {
        LimpezaCarrinhoResponseDTO r = service.executar();
        log.info("Limpeza de carrinhos: {} inativo(s), {} item(ns) indisponivel(is), {} vazio(s) removido(s)",
                r.carrinhosInativosRemovidos(), r.itensIndisponiveisRemovidos(), r.carrinhosVaziosRemovidos());
    }
}
