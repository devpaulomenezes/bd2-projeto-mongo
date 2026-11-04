package br.edu.ifpb.es.bd2.service;

import br.edu.ifpb.es.bd2.dto.CategoriaRequest;
import br.edu.ifpb.es.bd2.dto.CategoriaResponse;
import br.edu.ifpb.es.bd2.exception.NomeDuplicadoException;
import br.edu.ifpb.es.bd2.exception.RecursoNaoEncontradoException;
import br.edu.ifpb.es.bd2.model.Categoria;
import br.edu.ifpb.es.bd2.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaResponse criar(CategoriaRequest req) {
        String nome = req.nome().trim();
        if (repository.existsByNomeIgnoreCase(nome)) {
            throw new NomeDuplicadoException(nome);
        }
        Categoria categoria = Categoria.builder()
                .nome(nome)
                .descricao(req.descricao())
                .build();
        try {
            return toResponse(repository.save(categoria));
        } catch (DuplicateKeyException e) {
            throw new NomeDuplicadoException(nome);
        }
    }

    public List<CategoriaResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public CategoriaResponse buscarPorId(String id) {
        return toResponse(buscarEntidade(id));
    }

    public CategoriaResponse atualizar(String id, CategoriaRequest req) {
        Categoria categoria = buscarEntidade(id);
        String nome = req.nome().trim();

        if (repository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new NomeDuplicadoException(nome);
        }
        categoria.setNome(nome);
        categoria.setDescricao(req.descricao());
        try {
            return toResponse(repository.save(categoria));
        } catch (DuplicateKeyException e) {
            throw new NomeDuplicadoException(nome);
        }
    }

    public void deletar(String id) {
        repository.delete(buscarEntidade(id));
    }

    private Categoria buscarEntidade(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }

    private CategoriaResponse toResponse(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNome(), c.getDescricao());
    }
}