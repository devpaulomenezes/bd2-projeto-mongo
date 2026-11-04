package br.edu.ifpb.es.bd2.controller;

import br.edu.ifpb.es.bd2.dto.MensagemResponseDTO;
import br.edu.ifpb.es.bd2.dto.UsuarioRequestDTO;
import br.edu.ifpb.es.bd2.dto.UsuarioResponseDTO;
import br.edu.ifpb.es.bd2.model.Usuario;
import br.edu.ifpb.es.bd2.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody UsuarioRequestDTO request) {
        try {
            Usuario usuarioCriado = usuarioService.criar(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(usuarioCriado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MensagemResponseDTO(e.getMessage())); // 400 em caso de email
                                                                                              // duplicado
        }
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        List<UsuarioResponseDTO> usuarios = usuarioService.listar().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable String id) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
        return usuarioOpt.map(usuario -> ResponseEntity.ok(toResponseDTO(usuario)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable String id) {
        try {
            usuarioService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MensagemResponseDTO(e.getMessage()));
        }
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setRole(usuario.getRole());
        dto.setEnderecos(usuario.getEnderecos());
        dto.setCriadoEm(usuario.getCriadoEm());
        dto.setAtivo(usuario.getAtivo());
        return dto;
    }

    // Tratamento inline das falhas de validacao do @Valid (lançadas antes da
    // execucao do metodo criar).
    // Atualmente acionado apenas pelo criar(); novos endpoints com @Valid nesta
    // classe herdarão este tratamento.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensagemResponseDTO("Dados de usuário inválidos. Verifique nome, email e role."));
    }
}
