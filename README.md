# MS-AV-01 — Play Your List

Implementação do exercício de endpoints de músicas, playlists, reproduções e API orquestradora. Todos os endpoints ficam em **um único projeto Spring Boot**, como solicitado no enunciado. Os quatro domínios estão separados por pacotes; as chamadas da API orquestradora usam HTTP real com **OpenFeign**, inclusive quando apontam para a própria aplicação.

## Executar

Requisito: JDK 17 ou superior, com `JAVA_HOME` configurado. O Maven Wrapper incluído baixa o Maven automaticamente na primeira execução, que requer internet.

No PowerShell, dentro desta pasta:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

Base da API: `http://localhost:8080`. Para testar no navegador, abra `/musicas` ou `/playlists`. Não há frontend neste exercício.

Para testar e gerar o JAR:

```powershell
.\mvnw.cmd clean verify
java -jar target/ms-av-01-0.0.1-SNAPSHOT.jar
```

Os testes iniciam a aplicação em uma porta disponível e fazem requisições HTTP, incluindo os fluxos Feign. Cada cenário restaura os dados iniciais. A compilação usa Java 17; o desenvolvimento foi validado com JDK 21.

Para manter também os downloads do Maven dentro da pasta do projeto:

```powershell
$env:MAVEN_USER_HOME = "$PWD/.local/maven"
.\mvnw.cmd '-Dmaven.repo.local=.local/repository' verify
```

## Endpoints

| Método | Caminho | Resultado |
| --- | --- | --- |
| POST | `/musicas` | Cadastra música, 201 |
| GET | `/musicas` | Lista músicas, 200 |
| GET | `/musicas/{id}` | Busca música, 200 |
| PUT | `/musicas/{id}` | Atualiza música, 200 |
| DELETE | `/musicas/{id}` | Exclui música e seus vínculos, 204 |
| POST | `/playlists` | Cria playlist, 201 |
| GET | `/playlists` | Lista playlists, 200 |
| GET | `/playlists/{playlistid}` | Busca playlist, 200 |
| PUT | `/playlists/{playlistid}` | Atualiza nome e descrição, 200 |
| DELETE | `/playlists/{playlistid}` | Exclui playlist, vínculos e reproduções, 204 |
| POST | `/playlists/{playlistid}/musicas/{musicaId}` | Associa música existente, 201 |
| DELETE | `/playlists/{playlistid}/musicas/{musicaId}` | Remove associação, 204 |
| GET | `/playlists/{playlistid}/musicas` | Lista IDs das músicas por ordem de inclusão, 200 |
| POST | `/reproducao` | Registra execução com data/hora do servidor, 201 |
| GET | `/reproducao/{playlistid}` | Lista reproduções em ordem cronológica, 200 |
| GET | `/reproducao/total/{playlistid}` | Retorna um número com o total, 200 |
| POST | `/statistic` | Alias de cadastro de reprodução, 201 |
| POST | `/api/adicionar/{playlistId}/musicas/{musicaId}` | Valida e associa via Feign; retorna mensagem, 200 |
| PUT | `/api/executar/{playlistId}` | Valida playlist e registra execução via Feign, 200 |

### Corpos das requisições

Música (`POST` e `PUT`):

```json
{
  "titulo": "Hotel California",
  "artista": "Eagles",
  "album": "Hotel California",
  "duracao": 391,
  "genero": "Rock"
}
```

Playlist (`POST` e `PUT`):

```json
{
  "nome": "Músicas para trabalhar",
  "descricao": "Seleção para concentração"
}
```

Reprodução (`POST /reproducao` ou `POST /statistic`):

```json
{"playlistid": 1}
```

O cliente não precisa enviar ID nem data/hora da reprodução. A API gera ambos. O `PUT` de música/playlist substitui os campos do recurso; campos opcionais omitidos ficam nulos. IDs enviados no cadastro são ignorados; na atualização prevalece o ID do caminho.

Os endpoints de associação e da API orquestradora não precisam de corpo. A mensagem de sucesso da adição é texto: `Música Billie Jean adicionada com sucesso à playlist Música Brasileira`.

### Exemplo completo no PowerShell

Com a aplicação iniciada e os dados iniciais:

```powershell
Invoke-RestMethod http://localhost:8080/musicas
Invoke-RestMethod -Method Post http://localhost:8080/api/adicionar/2/musicas/2
Invoke-RestMethod http://localhost:8080/playlists/2/musicas
Invoke-RestMethod -Method Put http://localhost:8080/api/executar/2
Invoke-RestMethod http://localhost:8080/reproducao/total/2
```

O total da playlist 2 passa de 3 para 4. Repetir a inclusão da mesma música retorna 409. O arquivo `requisicoes.http` traz exemplos de todos os endpoints para clientes HTTP compatíveis.

## Validação e erros

- Música: título e artista obrigatórios, sem aceitar apenas espaços, até 150 caracteres; duração obrigatória e positiva; álbum opcional até 150 e gênero opcional até 50 caracteres.
- Playlist: nome obrigatório, sem aceitar apenas espaços, até 100 caracteres; descrição opcional até 255 caracteres.
- Reprodução: ID da playlist obrigatório, positivo e existente.
- Recurso ou associação inexistente: 404. Duplicidade de associação: 409, protegida também por restrição única no banco.
- Campos inválidos ou JSON malformado: 400. A validação retorna um mapa de campos e mensagens em português.
- Falha na comunicação Feign: 502; erros 400, 404 e 409 dos serviços são traduzidos para o mesmo status na API.

## Banco e decisões do exercício

H2 em memória, inicializado por `src/main/resources/data.sql`: 5 músicas, 5 playlists, 10 associações e 12 reproduções. Totais iniciais por playlist: **5, 3, 2, 1 e 1**. Os dados são reiniciados a cada execução da aplicação.

Adaptações necessárias do enunciado:

1. O texto especifica `/reproducao`, mas pede `/statistic` na orquestração. Ambos cadastram a mesma entidade; o cliente Feign usa `/statistic` literalmente.
2. A ordem dos `DROP TABLE` foi corrigida para remover tabelas dependentes primeiro.
3. Foram adicionadas integridade referencial de reproduções, exclusão em cascata das associações e uma restrição única para o par playlist/música. Excluir playlist preserva as músicas do catálogo e remove seu histórico de reprodução. Excluir música preserva as playlists.
4. “Executar” registra uma execução da playlist, conforme o exercício; não reproduz áudio. Cada chamada gera um novo registro, inclusive em playlist vazia.
5. Os limites de título, artista e nome acompanham também o tamanho das colunas SQL para evitar erros de persistência.

`PORT` altera a porta HTTP. `SERVICOS_URL` altera a URL base dos clientes Feign; por padrão, aponta para `http://localhost:<porta configurada>`.

## Referência e versões

Estrutura baseada em [esensato/ms-2026-02](https://github.com/esensato/ms-2026-02), revisão `6c130c2341b90044cd95bff28b1afd7d4b975f8c`: `AlunoController`, `CrudRepository`, entidades com campos públicos, injeção por construtor, `Optional` explícito e tratamento com `@RestControllerAdvice`.

Java 17 e Spring Boot 4.1.1 foram mantidos. Spring Cloud 2025.1.3 gerencia OpenFeign 5.0.3; a linha 2025.1.x suporta Boot 4.1.x a partir de 2025.1.2, conforme a [matriz oficial](https://spring.io/projects/spring-cloud/). O starter MVC específico do Boot 4 é utilizado para os controllers HTTP. Nenhum arquivo da referência é necessário para compilar este repositório.

## Automação de testes

O modelo em docs/github-actions-testes.yml pode ser colocado em .github/workflows/testes.yml para executar os testes no GitHub com JDK 17. Ele não está ativado: a credencial usada para criar o repositório não tem permissão workflow, e o GitHub rejeitou sua publicação nesse caminho. Os testes podem ser executados localmente pelos comandos acima.
