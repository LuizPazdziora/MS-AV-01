package aula.microservicos.playyourlist;

import java.net.ServerSocket;
import java.net.URI;
import java.net.http.*;
import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql("/data.sql")
class EndpointsTest {
    private static final int PORTA = portaDisponivel();
    private final HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json = new ObjectMapper();
    private static final String MUSICA = """
            {"titulo":"Hotel California","artista":"Eagles","album":"Hotel California","duracao":391,"genero":"Rock"}
            """;

    private static int portaDisponivel() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException erro) {
            throw new IllegalStateException(erro);
        }
    }

    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry registro) {
        registro.add("server.port", () -> PORTA);
        registro.add("servicos.url", () -> "http://localhost:" + PORTA);
    }

    private HttpResponse<String> chamar(String metodo, String caminho, String corpo, int status) throws Exception {
        HttpRequest requisicao = HttpRequest.newBuilder(URI.create("http://localhost:" + PORTA + caminho))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .method(metodo, HttpRequest.BodyPublishers.ofString(corpo)).build();
        HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());
        assertEquals(status, resposta.statusCode(), metodo + " " + caminho + ": " + resposta.body());
        return resposta;
    }

    private JsonNode obter(String caminho) throws Exception {
        return json.readTree(chamar("GET", caminho, "", 200).body());
    }

    @Test
    void dadosIniciais() throws Exception {
        assertEquals(5, obter("/musicas").size());
        assertEquals(5, obter("/playlists").size());
        assertEquals(3, obter("/playlists/1/musicas").size());
        assertEquals(5, obter("/reproducao/1").size());
        assertEquals(5, obter("/reproducao/total/1").asInt());
        assertEquals("Imagine", obter("/musicas/1").get("titulo").asText());
    }

    @Test
    void crudMusicasPreservaIdDaUrl() throws Exception {
        JsonNode criada = json.readTree(chamar("POST", "/musicas", MUSICA, 201).body());
        int id = criada.get("id").asInt();
        assertTrue(id > 5);
        String atualizada = MUSICA.replace("Hotel California", "Nova música").replace("{", "{\"id\":1,");
        chamar("PUT", "/musicas/" + id, atualizada, 200);
        assertEquals("Nova música", obter("/musicas/" + id).get("titulo").asText());
        assertEquals("Imagine", obter("/musicas/1").get("titulo").asText());
        chamar("DELETE", "/musicas/" + id, "", 204);
        chamar("GET", "/musicas/" + id, "", 404);
    }

    @Test
    void cadastroNaoSobrescreveIdInformado() throws Exception {
        chamar("POST", "/musicas", MUSICA.replace("{", "{\"id\":1,"), 201);
        chamar("POST", "/playlists", "{\"id\":1,\"nome\":\"Outra\"}", 201);
        assertEquals("Imagine", obter("/musicas/1").get("titulo").asText());
        assertEquals("Clássicos do Rock", obter("/playlists/1").get("nome").asText());
    }

    @Test
    void crudPlaylistsEVinculos() throws Exception {
        int id = json.readTree(chamar("POST", "/playlists", "{\"nome\":\"Teste\"}", 201).body()).get("id").asInt();
        chamar("PUT", "/playlists/" + id, "{\"nome\":\"Atualizada\",\"descricao\":\"Descrição\"}", 200);
        assertEquals("Atualizada", obter("/playlists/" + id).get("nome").asText());
        assertEquals(0, obter("/reproducao/total/" + id).asInt());
        chamar("POST", "/playlists/" + id + "/musicas/1", "", 201);
        chamar("POST", "/playlists/" + id + "/musicas/1", "", 409);
        assertEquals(1, obter("/playlists/" + id + "/musicas").get(0).asInt());
        chamar("DELETE", "/playlists/" + id + "/musicas/1", "", 204);
        assertEquals(0, obter("/playlists/" + id + "/musicas").size());
        chamar("DELETE", "/playlists/" + id + "/musicas/1", "", 404);
        chamar("POST", "/playlists/" + id + "/musicas/2", "", 201);
        chamar("POST", "/reproducao", "{\"playlistid\":" + id + "}", 201);
        chamar("DELETE", "/playlists/" + id, "", 204);
        chamar("GET", "/playlists/" + id, "", 404);
        assertEquals(5, obter("/musicas").size());
        assertEquals(5, obter("/playlists").size());
    }

    @Test
    void excluirMusicaRemoveVinculos() throws Exception {
        chamar("DELETE", "/musicas/1", "", 204);
        assertEquals(2, obter("/playlists/1/musicas").size());
        assertEquals(1, obter("/playlists/4/musicas").size());
        assertEquals(5, obter("/playlists").size());
    }

    @Test
    void orquestracaoUsaFeignComHttpReal() throws Exception {
        String mensagem = chamar("POST", "/api/adicionar/2/musicas/2", "", 200).body();
        assertEquals("Música Billie Jean adicionada com sucesso à playlist Música Brasileira", mensagem);
        assertEquals(2, obter("/playlists/2/musicas").size());
        chamar("POST", "/api/adicionar/2/musicas/2", "", 409);
        JsonNode execucao = json.readTree(chamar("PUT", "/api/executar/2", "", 200).body());
        assertEquals(2, execucao.get("playlistid").asInt());
        assertNotNull(execucao.get("datahora"));
        assertEquals(4, obter("/reproducao/total/2").asInt());
    }

    @Test
    void reproducaoEAliasGeramDataNoServidor() throws Exception {
        chamar("POST", "/reproducao", "{\"playlistid\":1}", 201);
        chamar("POST", "/statistic", "{\"playlistid\":1}", 201);
        assertEquals(7, obter("/reproducao/total/1").asInt());
        assertEquals(7, obter("/reproducao/1").size());
        chamar("POST", "/reproducao", "{}", 400);
        chamar("POST", "/reproducao", "{\"playlistid\":0}", 400);
        chamar("POST", "/reproducao", "{\"playlistid\":999}", 404);
    }

    @Test
    void recursosAusentesNaoProduzemEfeitos() throws Exception {
        chamar("PUT", "/musicas/999", MUSICA, 404);
        chamar("DELETE", "/musicas/999", "", 404);
        chamar("PUT", "/playlists/999", "{\"nome\":\"Teste\"}", 404);
        chamar("DELETE", "/playlists/999", "", 404);
        chamar("GET", "/playlists/999/musicas", "", 404);
        chamar("GET", "/reproducao/999", "", 404);
        chamar("GET", "/reproducao/total/999", "", 404);
        chamar("POST", "/playlists/1/musicas/999", "", 404);
        chamar("POST", "/playlists/999/musicas/1", "", 404);
        chamar("POST", "/api/adicionar/1/musicas/999", "", 404);
        chamar("POST", "/api/adicionar/999/musicas/1", "", 404);
        chamar("PUT", "/api/executar/999", "", 404);
        assertEquals(3, obter("/playlists/1/musicas").size());
        assertEquals(5, obter("/reproducao/total/1").asInt());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{}", "{\"titulo\":\" \",\"artista\":\"Eagles\",\"duracao\":1}",
        "{\"titulo\":\"Título\",\"artista\":\" \",\"duracao\":1}",
        "{\"titulo\":\"Título\",\"artista\":\"Eagles\"}",
        "{\"titulo\":\"Título\",\"artista\":\"Eagles\",\"duracao\":0}",
        "{\"titulo\":\"Título\",\"artista\":\"Eagles\",\"duracao\":-1}"
    })
    void validaMusicaNoCadastroEAtualizacao(String corpo) throws Exception {
        chamar("POST", "/musicas", corpo, 400);
        chamar("PUT", "/musicas/1", corpo, 400);
        assertEquals("Imagine", obter("/musicas/1").get("titulo").asText());
    }

    @Test
    void limitesDeCamposMusica() throws Exception {
        for (String campo : new String[]{"titulo", "artista", "album", "genero"}) {
            int limite = campo.equals("genero") ? 50 : 150;
            tools.jackson.databind.node.ObjectNode corpo = (tools.jackson.databind.node.ObjectNode) json.readTree(MUSICA);
            corpo.put(campo, "x".repeat(limite + 1));
            chamar("POST", "/musicas", corpo.toString(), 400);
            chamar("PUT", "/musicas/1", corpo.toString(), 400);
            corpo.put(campo, "x".repeat(limite));
            chamar("PUT", "/musicas/1", corpo.toString(), 200);
        }
        chamar("POST", "/musicas", "{\"titulo\":\"T\",\"artista\":\"A\",\"duracao\":1}", 201);
    }

    @Test
    void validaPlaylist() throws Exception {
        for (String corpo : new String[]{"{}", "{\"nome\":\" \"}",
                "{\"nome\":\"" + "x".repeat(101) + "\"}",
                "{\"nome\":\"Teste\",\"descricao\":\"" + "x".repeat(256) + "\"}"}) {
            chamar("POST", "/playlists", corpo, 400);
            chamar("PUT", "/playlists/1", corpo, 400);
        }
        chamar("POST", "/playlists", "{\"nome\":\"" + "x".repeat(100)
                + "\",\"descricao\":\"" + "x".repeat(255) + "\"}", 201);
    }

    @Test
    void jsonEParametroInvalidos() throws Exception {
        chamar("POST", "/musicas", "{", 400);
        chamar("GET", "/musicas/abc", "", 400);
        chamar("POST", "/reproducao", "{\"playlistid\":\"abc\"}", 400);
    }
}
