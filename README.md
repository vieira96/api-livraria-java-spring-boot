# Library API

API REST para cadastro e consulta de autores e livros, com cadastro de usuários. O projeto foi feito para praticar uma aplicação Spring Boot com PostgreSQL, migrations, validação, filtros, paginação e testes automatizados.

## Tecnologias

- Java 21
- Spring Boot
- Spring Data JPA
- Spring Security Crypto (BCrypt)
- PostgreSQL 16
- Flyway
- Docker Compose
- Testcontainers
- JUnit 5 e Mockito
- Maven

## Rodando com Docker

Você pode preparar o ambiente e subir o projeto com:

```bash
bash run-project.sh
```

O script cria o `.env` a partir do `.env.example` quando necessário, valida o Docker e inicia a API com o PostgreSQL em modo Watch. Mantenha o terminal aberto enquanto estiver desenvolvendo; use `Ctrl+C` para encerrar.

Se preferir rodar manualmente, crie o arquivo de ambiente a partir do exemplo:

```bash
cp .env.example .env
```

No `.env`, as variáveis `POSTGRES_*` configuram o container do banco e as `DB_*` são usadas pela API para se conectar. Mantenha `POSTGRES_USER` e `DB_USERNAME` iguais, assim como `POSTGRES_PASSWORD` e `DB_PASSWORD`. A `DB_URL` também deve apontar para o banco definido em `POSTGRES_DB`.

Depois suba a API e o banco:

```bash
docker compose --env-file .env -f docker/docker-compose.yml up --build
```

Esse comando sobe o PostgreSQL e inicia a API com Maven e Spring Boot DevTools na porta definida por `SERVER_PORT` no `.env` (`8000` se ela não for informada). Não é necessário instalar Java ou Maven na máquina; basta ter Docker ou Docker Desktop.

A porta da API é definida por `SERVER_PORT` no `.env`. Com o valor padrão (`8000`), ela fica disponível em `http://localhost:8000/api`.

Para parar os containers:

```bash
docker compose --env-file .env -f docker/docker-compose.yml down
```

## Desenvolvimento

O projeto inclui o Spring Boot DevTools. Para desenvolver com atualização automática após salvar arquivos em `src`, inicie com Docker Compose Watch:

```bash
docker compose --env-file .env -f docker/docker-compose.yml up --build --watch
```

Alterações em `src` são sincronizadas e reiniciam a API. Mudanças no `pom.xml` disparam um novo build da imagem.

As tabelas são criadas e versionadas pelo Flyway na inicialização.

## Documentação da API

A API usa OpenAPI, gerado pelo Springdoc, e a interface Scalar para a documentação interativa.

No `.env`, o valor padrão de `SERVER_PORT` é `8000`. Se você alterá-lo, use o mesmo valor nas URLs abaixo.

```text
http://localhost:8000/scalar
```

O documento OpenAPI em JSON fica em:

```text
http://localhost:8000/v3/api-docs
```

## Endpoints principais

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/auth/register` | Cadastra um usuário |
| `POST` | `/authors` | Cria um autor |
| `GET` | `/authors` | Lista autores com filtros e paginação |
| `GET` | `/authors/{id}` | Busca um autor |
| `PUT` | `/authors/{id}` | Atualiza um autor |
| `DELETE` | `/authors/{id}` | Remove um autor sem livros vinculados |
| `POST` | `/books` | Cria um livro |
| `GET` | `/books` | Lista livros com filtros e paginação |
| `GET` | `/books/{id}` | Busca um livro |
| `PUT` | `/books/{id}` | Atualiza um livro |
| `DELETE` | `/books/{id}` | Remove um livro |

Todas as rotas da tabela usam o prefixo `/api`.

### Cadastro de usuário

O módulo de autenticação disponibiliza apenas o cadastro por enquanto. Login, geração de token e autorização ainda não foram implementados.

```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Maria Silva",
  "email": "maria@example.com",
  "password": "senha-segura"
}
```

Em caso de sucesso, a API responde com `201 Created`:

```json
{
  "id": "UUID_DO_USUARIO",
  "name": "Maria Silva",
  "email": "maria@example.com",
  "roles": ["USER"],
  "createdAt": "2026-09-14T19:35",
  "updatedAt": "2026-09-14T19:35"
}
```

O nome e o e-mail têm espaços externos removidos, e o e-mail é armazenado em letras minúsculas. A senha deve ter entre 8 e 72 caracteres, é persistida como hash BCrypt e nunca é retornada pela API. Tentativas de cadastrar o mesmo e-mail, sem diferenciar maiúsculas e minúsculas, recebem `409 Conflict`.

As roles padrão `ADMIN` e `USER` são criadas pelo Flyway. Todo usuário cadastrado pela API recebe automaticamente a role `USER`. A resposta expõe somente os nomes das roles, sem os IDs nem os dados da tabela associativa `user_roles`.

Exemplo para criar um autor:

```http
POST /api/authors
Content-Type: application/json

{
  "name": "Machado de Assis",
  "birthdate": "1839-06-21",
  "nationality": "Brasileira"
}
```

Exemplo para criar um livro:

```http
POST /api/books
Content-Type: application/json

{
  "title": "Dom Casmurro",
  "isbn": "978-85-359-0277-8",
  "publishDate": "1899-01-01",
  "gender": "FICCAO",
  "price": 49.90,
  "authorId": "UUID_DO_AUTOR"
}
```

## Filtros, paginação e ordenação

As listagens aceitam `page` e `size`. A página começa em `1`; o tamanho padrão é `2` e o máximo é `100`.

Autores podem ser filtrados por `name`, `nationality` e `search`. Livros aceitam `title`, `isbn`, `gender`, `authorId` e `search`.

Exemplos:

```http
GET /api/authors?page=1&size=10&search=machado&sortBy=NAME&direction=ASC
GET /api/books?page=1&size=10&gender=FICCAO&sortBy=PUBLISH_DATE&direction=DESC
```

## Contagem de livros por autor

O campo `bookCount` é opcional. Para incluí-lo na resposta, use:

```http
GET /api/authors?include=bookCount
GET /api/authors/UUID_DO_AUTOR?include=bookCount
```

A contagem é feita com uma consulta agregada para os autores retornados na página, sem carregar a lista de livros de cada autor. Isso evita o problema de N+1 queries.

## Testes

Os testes de repository e os testes de integração usam um PostgreSQL temporário criado pelo Testcontainers. Eles não alteram o banco configurado no `.env`. Para executar toda a suíte, mantenha o Docker ativo e rode na raiz do projeto:

```bash
./mvnw test
```

Para executar apenas os testes de autenticação:

```bash
./mvnw -Dtest='AuthService*Test' test
```

O teste unitário do serviço usa JUnit 5 e Mockito, não inicializa o Spring e não precisa de Docker:

```bash
./mvnw -Dtest=AuthServiceUnitTest test
```

Para executar somente o teste de integração do cadastro, o Docker deve estar ativo:

```bash
./mvnw -Dtest=AuthServiceTest test
```

## Postman

A collection está em [postman/Library API.postman_collection.json](postman/Library%20API.postman_collection.json). Importe o arquivo no Postman e ajuste a variável `baseUrl` para `http://localhost:SERVER_PORT/api`, usando o valor definido no seu `.env`.

A pasta `Authentication` contém requisições para cadastro válido, e-mail duplicado e dados inválidos. Execute-as na ordem apresentada: o primeiro cadastro gera um e-mail único e o armazena em `registrationEmail`, utilizado pelo cenário de duplicidade.
