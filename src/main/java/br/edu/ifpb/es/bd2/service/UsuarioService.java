package br.edu.ifpb.es.bd2.service;

import br.edu.ifpb.es.bd2.dto.UsuarioRequestDTO;
import br.edu.ifpb.es.bd2.model.Usuario;
import br.edu.ifpb.es.bd2.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario criar(UsuarioRequestDTO dto) {
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário com o email informado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setRole(dto.getRole());
        usuario.setEnderecos(dto.getEnderecos());

        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                .collect(Collectors.toList());
    }

    public Optional<Usuario> buscarPorId(String id) {
        return usuarioRepository.findById(id)
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()));
    }

    public void deletar(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado."));

        if (Boolean.FALSE.equals(usuario.getAtivo())) {
            throw new IllegalArgumentException("Usuário já está inativo.");
        }

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }
}
