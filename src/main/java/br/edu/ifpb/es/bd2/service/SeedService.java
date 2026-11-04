package br.edu.ifpb.es.bd2.service;

import br.edu.ifpb.es.bd2.model.Categoria;
import br.edu.ifpb.es.bd2.model.EnderecoEmbedded;
import br.edu.ifpb.es.bd2.model.Produto;
import br.edu.ifpb.es.bd2.model.Role;
import br.edu.ifpb.es.bd2.model.Usuario;
import br.edu.ifpb.es.bd2.repository.CategoriaRepository;
import br.edu.ifpb.es.bd2.repository.ProdutoRepository;
import br.edu.ifpb.es.bd2.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class SeedService {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
    
    // ATENCAO: os repositories abaixo ainda nao existem no projeto. Conforme novas
    // colecoes forem implementadas (carrinhos, cupons, pedidos, avaliacoes),
    // descomentar a injecao correspondente, o deleteAll() em limparTudo() e a populacao
    // correspondente em popularBanco(). Este SeedService deve ser ajustado a cada
    // progressao do projeto.
    // private final CarrinhoRepository carrinhoRepository;
    // private final CupomRepository cupomRepository;
    // private final PedidoRepository pedidoRepository;
    // private final AvaliacaoRepository avaliacaoRepository;

    public SeedService(UsuarioRepository usuarioRepository, CategoriaRepository categoriaRepository,
                       ProdutoRepository produtoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
    }

    public void limparTudo() {
        usuarioRepository.deleteAll();
        categoriaRepository.deleteAll();
        produtoRepository.deleteAll();
        // carrinhoRepository.deleteAll();
        // cupomRepository.deleteAll();
        // pedidoRepository.deleteAll();
        // avaliacaoRepository.deleteAll();
    }

    public void popularBanco() {
        // População de Categorias
        List<Categoria> categorias = new ArrayList<>();
        categorias.add(Categoria.builder().nome("Eletrônicos").descricao("Dispositivos eletrônicos em geral").build());
        categorias.add(Categoria.builder().nome("Informática").descricao("Computadores, peças e acessórios").build());
        categorias.add(Categoria.builder().nome("Livros").descricao("Livros impressos e digitais").build());
        categorias.add(Categoria.builder().nome("Moda").descricao("Roupas, calçados e acessórios").build());
        categorias.add(Categoria.builder().nome("Casa e Cozinha").descricao("Utensílios, móveis e decoração").build());
        categorias.add(Categoria.builder().nome("Esporte e Lazer").descricao("Artigos esportivos e suplementos").build());
        categorias = categoriaRepository.saveAll(categorias);

        // População de Usuários
        List<Usuario> usuarios = new ArrayList<>();

        // 1 ADMIN
        usuarios.add(criarUsuario("Admin Master", "admin@marketplace.com", Role.ADMIN));

        // 2 VENDEDOR
        usuarios.add(criarUsuario("Vendedor João", "joao.vendedor@marketplace.com", Role.VENDEDOR));
        usuarios.add(criarUsuario("Vendedora Maria", "maria.vendedora@marketplace.com", Role.VENDEDOR));

        // 5 CLIENTE
        usuarios.add(criarUsuario("Cliente Carlos", "carlos.cliente@email.com", Role.CLIENTE));
        usuarios.add(criarUsuario("Cliente Ana", "ana.cliente@email.com", Role.CLIENTE));
        usuarios.add(criarUsuario("Cliente Pedro", "pedro.cliente@email.com", Role.CLIENTE));
        usuarios.add(criarUsuario("Cliente Julia", "julia.cliente@email.com", Role.CLIENTE));
        usuarios.add(criarUsuario("Cliente Roberto", "roberto.cliente@email.com", Role.CLIENTE));

        usuarios = usuarioRepository.saveAll(usuarios);

        // População de Produtos
        Usuario joao = usuarios.get(1);
        Usuario maria = usuarios.get(2);
        List<Produto> produtos = new ArrayList<>();
        produtos.add(criarProduto("Smartphone X10", "Tela de 6,5 polegadas e 128 GB", "2499.90", 25, categorias.get(0), joao));
        produtos.add(criarProduto("Notebook Pro 14", "16 GB de RAM e SSD de 512 GB", "4599.90", 10, categorias.get(1), joao));
        produtos.add(criarProduto("Clean Code", "Livro sobre boas práticas de programação", "89.90", 40, categorias.get(2), maria));
        produtos.add(criarProduto("Tênis de Corrida", "Tênis leve para treinos diários", "299.90", 30, categorias.get(3), maria));
        produtos.add(criarProduto("Air Fryer 4L", "Fritadeira elétrica sem óleo", "399.90", 15, categorias.get(4), joao));
        produtos.add(criarProduto("Bola de Futebol", "Bola oficial tamanho 5", "129.90", 50, categorias.get(5), maria));
        produtoRepository.saveAll(produtos);

        // População futura de carrinhos, cupons, pedidos e avaliações...
    }

    private Produto criarProduto(String nome, String descricao, String preco, int estoque, Categoria categoria, Usuario vendedor) {
        return Produto.builder()
                .nome(nome)
                .descricao(descricao)
                .preco(new BigDecimal(preco))
                .estoque(estoque)
                .categoriaId(categoria.getId())
                .categoriaNome(categoria.getNome())
                .vendedorId(vendedor.getId())
                .vendedorNome(vendedor.getNome())
                .build();
    }

    private Usuario criarUsuario(String nome, String email, Role role) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setRole(role);
        
        EnderecoEmbedded endereco = new EnderecoEmbedded();
        endereco.setLogradouro("Rua Fictícia");
        endereco.setNumero("123");
        endereco.setBairro("Centro");
        endereco.setCidade("João Pessoa");
        endereco.setEstado("PB");
        endereco.setCep("58000-000");
        
        usuario.setEnderecos(new ArrayList<>(List.of(endereco)));
        
        return usuario;
    }
}
