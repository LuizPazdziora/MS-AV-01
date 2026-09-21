package aula.microservicos.playyourlist.api;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import aula.microservicos.playyourlist.musicas.Musica;
import aula.microservicos.playyourlist.playlists.Playlist;
import aula.microservicos.playyourlist.reproducoes.Reproducao;
import aula.microservicos.playyourlist.reproducoes.ReproducaoPedido;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final MusicaClienteFeign musicas;
    private final PlaylistClienteFeign playlists;
    private final ReproducaoClienteFeign reproducoes;

    public ApiController(MusicaClienteFeign musicas, PlaylistClienteFeign playlists, ReproducaoClienteFeign reproducoes) {
        this.musicas = musicas;
        this.playlists = playlists;
        this.reproducoes = reproducoes;
    }

    @PostMapping("/adicionar/{playlistId}/musicas/{musicaId}")
    public ResponseEntity<String> adicionar(@PathVariable Integer playlistId, @PathVariable Integer musicaId) {
        Musica musica = musicas.buscar(musicaId);
        Playlist playlist = playlists.buscar(playlistId);
        playlists.adicionar(playlistId, musicaId);
        String mensagem = "Música " + musica.titulo + " adicionada com sucesso à playlist " + playlist.nome;
        return new ResponseEntity<String>(mensagem, HttpStatus.OK);
    }

    @PutMapping("/executar/{playlistId}")
    public ResponseEntity<Reproducao> executar(@PathVariable Integer playlistId) {
        playlists.buscar(playlistId);
        ReproducaoPedido pedido = new ReproducaoPedido();
        pedido.playlistid = playlistId;
        return new ResponseEntity<Reproducao>(reproducoes.registrar(pedido), HttpStatus.OK);
    }
}
