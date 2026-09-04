# Library API

API REST para cadastro e consulta de autores e livros. O projeto foi feito para praticar uma aplicação Spring Boot com PostgreSQL, migrations, filtros e paginação.

## Tecnologias

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL 16
- Flyway
- Docker Compose
- Maven

## Rodando com Docker

Crie o arquivo de ambiente a partir do exemplo:

```bash
cp .env.example .env
```

Confira no `.env` se `POSTGRES_PASSWORD` e `DB_PASSWORD` têm o mesmo valor. Depois suba a API e o banco:

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

Execute os testes no container de desenvolvimento:

```bash
docker compose --env-file .env -f docker/docker-compose.yml run --rm --build api ./mvnw test
```

## Postman

A collection está em [postman/Library API.postman_collection.json](postman/Library%20API.postman_collection.json). Importe o arquivo no Postman e ajuste a variável `baseUrl` para `http://localhost:SERVER_PORT/api`, usando o valor definido no seu `.env`.
