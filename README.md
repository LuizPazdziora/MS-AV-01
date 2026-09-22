# Play Your List

## Objetivo

Atividade MS-AV-01: cadastrar músicas, montar playlists e registrar suas reproduções. Todos os endpoints ficam em uma aplicação, separados por pacotes, seguindo os exemplos de [Microservices da disciplina](https://github.com/esensato/ms-2026-02).

## Estrutura do projeto

Os pacotes ficam em `src/main/java/playyourlist`:

```text
playyourlist/
├── PlayYourListApplication.java
├── api/
├── excecoes/
├── musicas/
├── playlists/
└── reproducoes/
```

- `musicas`: entidade, repository e controller de músicas.
- `playlists`: playlists e associações, com acesso direto aos repositories.
- `reproducoes`: cadastro, listagem e contagem de reproduções.
- `api`: controller e clientes OpenFeign para chamar os endpoints por HTTP.
- `excecoes`: tratamento simples de recursos inexistentes, duplicidades e validação.

`PlayYourListApplication.java` inicia a aplicação. O arquivo `src/main/resources/data.sql` cria as tabelas e inclui 5 músicas, 5 playlists, 10 associações e 12 reproduções. O H2 fica em memória e é reiniciado junto com a aplicação.

## Tecnologias utilizadas

- Java 17
- Spring Boot e Spring Web
- Spring Data JPA
- H2
- Bean Validation
- OpenFeign
- Maven
- JUnit e Spring Boot Test

## Como executar

Com JDK, Maven e extensões Java já instalados:

1. Abra a pasta `MS-AV-01` no VS Code e aguarde a importação do Maven.
2. Abra `src/main/java/playyourlist/PlayYourListApplication.java`.
3. Clique em **Run** acima do método `main`.
4. A API ficará disponível em `http://localhost:8080`.

O H2 e os dados iniciais são carregados automaticamente. Não é necessário iniciar um banco separado. A porta 8080 deve estar livre.

## Como testar

Abra `requisicoes.http` e use **Send Request** com a extensão REST Client do VS Code. Os exemplos estão agrupados por domínio. Nos exemplos de cadastro, use o ID retornado nas requisições seguintes.

Para testar o Feign, adicione a música 2 à playlist 2 em `/api/adicionar/2/musicas/2` e execute `/api/executar/2`. O total da playlist 2 passa de 3 para 4. Repetir a mesma associação retorna 409.

O teste `PlayYourListApplicationTests` verifica a inicialização do contexto Spring e pode ser executado pelo VS Code. Pelo terminal, também é possível executar mvn test.

## Endpoints

| Método | Caminho | Operação |
| --- | --- | --- |
| POST | `/musicas` | Cadastrar música |
| GET | `/musicas` | Listar músicas |
| GET | `/musicas/{id}` | Buscar música |
| PUT | `/musicas/{id}` | Atualizar música |
| DELETE | `/musicas/{id}` | Excluir música |
| POST | `/playlists` | Criar playlist |
| GET | `/playlists` | Listar playlists |
| GET | `/playlists/{playlistid}` | Buscar playlist |
| PUT | `/playlists/{playlistid}` | Atualizar nome e descrição |
| DELETE | `/playlists/{playlistid}` | Excluir playlist e suas associações |
| POST | `/playlists/{playlistid}/musicas/{musicaId}` | Associar música |
| DELETE | `/playlists/{playlistid}/musicas/{musicaId}` | Remover associação |
| GET | `/playlists/{playlistid}/musicas` | Listar IDs das músicas |
| POST | `/reproducao` | Registrar reprodução |
| GET | `/reproducao/{playlistid}` | Listar reproduções |
| GET | `/reproducao/total/{playlistid}` | Consultar total |
| POST | `/statistic` | Registrar reprodução, como `/reproducao` |
| POST | `/api/adicionar/{playlistId}/musicas/{musicaId}` | Validar e associar via OpenFeign |
| PUT | `/api/executar/{playlistId}` | Validar playlist e chamar `/statistic` via OpenFeign |

O cadastro de reprodução recebe `{"playlistid":1}`; a data/hora é gerada no servidor. Executar uma playlist registra uma reprodução, sem tocar áudio.

Título, artista e nome da playlist não podem estar em branco; duração é obrigatória e positiva. Os tamanhos máximos dos campos são validados. Entradas inválidas retornam 400, recursos inexistentes retornam 404 e associações duplicadas retornam 409.

Excluir uma playlist remove suas associações e reproduções, preservando as músicas do catálogo. Excluir uma música remove seus vínculos, preservando as playlists.
