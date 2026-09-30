package br.edu.ifpb.es.bd2.service;

import br.edu.ifpb.es.bd2.model.Categoria;
import br.edu.ifpb.es.bd2.model.EnderecoEmbedded;
import br.edu.ifpb.es.bd2.model.Role;
import br.edu.ifpb.es.bd2.model.Usuario;
import br.edu.ifpb.es.bd2.repository.CategoriaRepository;
import br.edu.ifpb.es.bd2.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeedService {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    
    // ATENCAO: os repositories abaixo ainda nao existem no projeto. Conforme novas
    // colecoes forem implementadas (produtos, carrinhos, cupons, pedidos, avaliacoes),
    // descomentar a injecao correspondente, o deleteAll() em limparTudo() e a populacao
    // correspondente em popularBanco(). Este SeedService deve ser ajustado a cada
    // progressao do projeto.
    // private final ProdutoRepository produtoRepository;
    // private final CarrinhoRepository carrinhoRepository;
    // private final CupomRepository cupomRepository;
    // private final PedidoRepository pedidoRepository;
    // private final AvaliacaoRepository avaliacaoRepository;

    public SeedService(UsuarioRepository usuarioRepository, CategoriaRepository categoriaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public void limparTudo() {
        usuarioRepository.deleteAll();
        categoriaRepository.deleteAll();
        // produtoRepository.deleteAll();
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
        categoriaRepository.saveAll(categorias);

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

        usuarioRepository.saveAll(usuarios);

        // População futura de produtos, carrinhos, cupons, pedidos e avaliações...
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
