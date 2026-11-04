package br.edu.ifpb.es.bd2.service;

import br.edu.ifpb.es.bd2.dto.CarrinhoResponseDTO;
import br.edu.ifpb.es.bd2.dto.ItemCarrinhoRequestDTO;
import br.edu.ifpb.es.bd2.exception.ClienteInvalidoException;
import br.edu.ifpb.es.bd2.exception.EstoqueInsuficienteException;
import br.edu.ifpb.es.bd2.exception.RecursoNaoEncontradoException;
import br.edu.ifpb.es.bd2.model.Carrinho;
import br.edu.ifpb.es.bd2.model.ItemCarrinhoEmbedded;
import br.edu.ifpb.es.bd2.model.Produto;
import br.edu.ifpb.es.bd2.model.Role;
import br.edu.ifpb.es.bd2.model.Usuario;
import br.edu.ifpb.es.bd2.repository.CarrinhoRepository;
import br.edu.ifpb.es.bd2.repository.ProdutoRepository;
import br.edu.ifpb.es.bd2.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarrinhoService {

    private final CarrinhoRepository repository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    public CarrinhoResponseDTO buscar(String clienteId) {
        validarCliente(clienteId);
        Carrinho carrinho = repository.findByClienteId(clienteId).orElseGet(() -> new Carrinho(clienteId));
        return CarrinhoResponseDTO.from(carrinho);
    }

    public CarrinhoResponseDTO adicionarItem(String clienteId, ItemCarrinhoRequestDTO req) {
        validarCliente(clienteId);
        Produto produto = buscarProduto(req.produtoId());
        Carrinho carrinho = repository.findByClienteId(clienteId).orElseGet(() -> new Carrinho(clienteId));

        ItemCarrinhoEmbedded item = localizarItem(carrinho, produto.getId());
        int quantidadeAtual = item != null ? item.getQuantidade() : 0;
        long quantidadeFinal = (long) quantidadeAtual + req.quantidade();
        garantirEstoque(produto, quantidadeFinal, req.quantidade());

        if (item == null) {
            item = new ItemCarrinhoEmbedded();
            item.setProdutoId(produto.getId());
            carrinho.getItens().add(item);
        }
        preencherItem(item, produto, (int) quantidadeFinal);
        return CarrinhoResponseDTO.from(salvar(carrinho));
    }

    public CarrinhoResponseDTO alterarQuantidade(String clienteId, String produtoId, int quantidade) {
        validarCliente(clienteId);
        Carrinho carrinho = buscarCarrinho(clienteId);
        ItemCarrinhoEmbedded item = buscarItem(carrinho, produtoId);
        Produto produto = buscarProduto(produtoId);

        garantirEstoque(produto, quantidade, quantidade);
        preencherItem(item, produto, quantidade);
        return CarrinhoResponseDTO.from(salvar(carrinho));
    }

    public CarrinhoResponseDTO removerItem(String clienteId, String produtoId) {
        validarCliente(clienteId);
        Carrinho carrinho = buscarCarrinho(clienteId);
        buscarItem(carrinho, produtoId);

        carrinho.getItens().removeIf(i -> produtoId.equals(i.getProdutoId()));
        return CarrinhoResponseDTO.from(salvar(carrinho));
    }

    public CarrinhoResponseDTO limpar(String clienteId) {
        validarCliente(clienteId);
        repository.deleteByClienteId(clienteId);
        return CarrinhoResponseDTO.from(new Carrinho(clienteId));
    }

    private Carrinho salvar(Carrinho carrinho) {
        carrinho.registrarAtividade();
        return repository.save(carrinho);
    }

    private void preencherItem(ItemCarrinhoEmbedded item, Produto produto, int quantidade) {
        item.setNome(produto.getNome());
        item.setPrecoUnitario(produto.getPreco());
        item.setQuantidade(quantidade);
    }

    private void garantirEstoque(Produto produto, long quantidadeFinal, int quantidadePedida) {
        if (produto.getEstoque() == null || quantidadeFinal > produto.getEstoque()) {
            throw new EstoqueInsuficienteException(produto.getId(), quantidadePedida);
        }
    }

    private Usuario validarCliente(String clienteId) {
        Usuario usuario = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", clienteId));
        if (usuario.getRole() != Role.CLIENTE || !Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new ClienteInvalidoException(clienteId);
        }
        return usuario;
    }

    private Produto buscarProduto(String id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
    }

    private Carrinho buscarCarrinho(String clienteId) {
        return repository.findByClienteId(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carrinho do cliente", clienteId));
    }

    private ItemCarrinhoEmbedded buscarItem(Carrinho carrinho, String produtoId) {
        ItemCarrinhoEmbedded item = localizarItem(carrinho, produtoId);
        if (item == null) {
            throw new RecursoNaoEncontradoException("Item do carrinho (produto)", produtoId);
        }
        return item;
    }

    private ItemCarrinhoEmbedded localizarItem(Carrinho carrinho, String produtoId) {
        return carrinho.getItens().stream()
                .filter(i -> produtoId.equals(i.getProdutoId()))
                .findFirst()
                .orElse(null);
    }
}
