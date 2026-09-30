package br.edu.ifpb.es.bd2.controller;

import br.edu.ifpb.es.bd2.dto.MensagemResponseDTO;
import br.edu.ifpb.es.bd2.service.SeedService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seed")
public class SeedController {

    private final SeedService seedService;

    public SeedController(SeedService seedService) {
        this.seedService = seedService;
    }

    @PostMapping("/reset")
    public ResponseEntity<MensagemResponseDTO> reset() {
        try {
            seedService.limparTudo();
            seedService.popularBanco();
            
            MensagemResponseDTO response = new MensagemResponseDTO(
                    "Base resetada com sucesso: dados iniciais de categorias e usuários criados. O seed será estendido às demais coleções futuramente."
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new MensagemResponseDTO("Falha ao comunicar com o banco de dados."));
        }
    }
}
