# 🍃 Marketplace NoSQL API — Banco de Dados II (BD2)

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-Atlas_NoSQL-47A248?style=for-the-badge&logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)

Este repositório contém a reengenharia e migração de uma API REST de Marketplace da disciplina de **Banco de Dados II (BD2)** do curso de **Análise e Desenvolvimento de Sistemas (ADS)**.

O objetivo central do projeto é realizar a transição de um modelo relacional clássico (PostgreSQL/JPA) de 16 tabelas para um **modelo NoSQL orientado a documentos**, aplicando conceitos avançados de modelagem agregada no **MongoDB com Spring Data MongoDB**.

---

## 📌 Contexto da Migração (Relacional ➔ NoSQL)

No modelo relacional tradicional, dados de uma mesma transação encontravam-se fragmentados em dezenas de tabelas para atender à 3ª Forma Normal (3FN), exigindo operações custosas de múltiplos `JOINs` para leitura.

Na arquitetura NoSQL com MongoDB, aplicamos o princípio: **"Dados que são acessados juntos devem ser armazenados juntos"**, consolidando as 16 tabelas originais em **7 coleções coesas e otimizadas**:

```
 ┌─────────────────────────────────────────────────────────────────────────────┐
 │                       MAPA DE TRANSIÇÃO DAS COLEÇÕES                        │
 ├─────────────────────────────────────────────────────────────────────────────┤
 │ • 1. usuarios   ➔ Consolida usuario, admin, cliente, vendedor e enderecos   │
 │ • 2. categorias ➔ Entidade independente de taxonomia                        │
 │ • 3. produtos   ➔ Catálogo com desnormalização de leitura e métricas        │
 │ • 4. carrinhos  ➔ Agregado de sessão com itens embutidos (elimina chave     │
 │                    composta associativa)                                    │
 │ • 5. cupons     ➔ Entidade independente de regras e descontos               │
 │ • 6. pedidos    ➔ Agregado raiz com SNAPSHOT fiscal imutável e subdocumentos│
 │                    de pagamento, entrega e devolução                        │
 │ • 7. avaliacoes ➔ Entidade paginada independente para alto volume           │
 └─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🧠 Padrões e Conceitos Chave Implementados

### 1. 📷 Snapshots Históricos (Fotografia Fiscal)
Em compras no e-commerce, o pedido representa um fato passado imutável. No momento do checkout, o **`Pedido`** congela uma cópia exata (snapshot) dos preços, descrições dos produtos, descontos aplicados e endereço de entrega. Se o vendedor alterar o preço do produto ou o cliente mudar de endereço no futuro, o histórico do pedido permanece intocado.

### 2. ⚡ Desnormalização de Leitura
Para garantir altíssimo throughput na vitrine do marketplace, a coleção de **`produtos`** armazena cópias dos nomes da categoria e do vendedor, além de métricas pré-calculadas (`mediaAvaliacoes` e `totalAvaliacoes`). A consulta de catálogo ocorre em **1 único acesso direto (Zero JOINs e Zero GROUP BY)**.

### 3. 📦 Subdocumentos Embutidos (Embedded Documents)
Extinção de tabelas associativas e classes de chaves compostas (como `ItemCarrinhoId` e `ItemPedidoId`). Estruturas com ciclo de vida estritamente acoplado (como itens de carrinho, itens de pedido, pagamento e entrega) são armazenadas como subdocumentos dentro do próprio documento pai.

### 4. 📄 Entidades Paginadas (Controle de Crescimento)
A coleção de **`avaliacoes`** é mantida como uma entidade independente e indexada por `produtoId`. Isso impede o problema de *Unbounded Array* (arrays infinitos que estouram a memória do produto), permitindo carregar avaliações em blocos (páginas de 10 em 10).

---

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Java 17+
* **Framework:** Spring Boot 3.x
* **Persistência NoSQL:** Spring Data MongoDB
* **Banco de Dados:** MongoDB Atlas (Cloud Database)
* **Produtividade & Validação:** Lombok, Jakarta Bean Validation
* **Gerenciador de Dependências:** Apache Maven

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
* Java JDK 17 ou superior instalado
* Maven 3.8+ instalado (ou utilizar o `./mvnw` embutido)
* Conta ativa no MongoDB Atlas (ou MongoDB Server local)

### 1. Clonar o repositório
```bash
git clone https://github.com/devpaulomenezes/bd2-projeto-mongo.git
cd bd2-projeto-mongo
```

### 2. Configurar a Conexão com o MongoDB
Abra o arquivo `src/main/resources/application.properties` e configure a sua URI de conexão do MongoDB:

```properties
spring.application.name=bd2-mongodb-api

# String de Conexão com o MongoDB Atlas
spring.data.mongodb.uri=mongodb+srv://<USUARIO>:<SENHA>@cluster0.xxxx.mongodb.net/bd2_db?retryWrites=true&w=majority&appName=Cluster0
```

### 3. Executar a Aplicação
Execute via terminal:
```bash
./mvnw spring-boot:run
```
Ou execute a classe principal `Bd2MongodbApiApplication.java` diretamente pela sua IDE favorita (IntelliJ IDEA, VS Code ou Eclipse).

A API estará disponível e pronta para receber requisições em: `http://localhost:8080`.

---

## 📂 Estrutura de Pacotes Sugerida

```text
src/main/java/br/edu/ifpb/es/bd2
├── config/             # Configurações do Spring e MongoDB
├── controller/         # Endpoints REST (Controllers)
├── dto/                # Data Transfer Objects (Request/Response)
├── exception/          # Tratamento global de exceções
├── model/              # Documentos MongoDB (@Document) e Subdocumentos
├── repository/         # Interfaces MongoRepository
└── service/            # Regras de negócio e casos de uso
```

---

## 👨‍💻👨‍💻👨‍💻👨‍💻 Autores

Desenvolvido por **Paulo Moura, Arthur Santos, Lucas Barbosa e Valdenio Pantaleão**  
*Estudante de Análise e Desenvolvimento de Sistemas (ADS) — IFPB*  
*GitHub:* [@devpaulomenezes](https://github.com/devpaulomenezes)
