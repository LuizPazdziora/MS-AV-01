package playyourlist.reproducoes;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import playyourlist.playlists.PlaylistRepository;
import playyourlist.excecoes.RecursoInexistenteException;

@RestController
public class ReproducaoController {
    private final ReproducaoRepository repo;
    private final PlaylistRepository playlists;

    public ReproducaoController(ReproducaoRepository repo, PlaylistRepository playlists) {
        this.repo = repo;
        this.playlists = playlists;
    }

    // O enunciado usa os dois caminhos para a mesma operação.
    @PostMapping({"/reproducao", "/statistic"})
    public ResponseEntity<Reproducao> registrar(@Valid @RequestBody ReproducaoPedido pedido) {
        verificarPlaylist(pedido.playlistid);
        Reproducao reproducao = new Reproducao();
        reproducao.playlistid = pedido.playlistid;
        reproducao.datahora = LocalDateTime.now();
        return new ResponseEntity<Reproducao>(repo.save(reproducao), HttpStatus.CREATED);
    }

    @GetMapping("/reproducao/{playlistid}")
    public ResponseEntity<List<Reproducao>> listar(@PathVariable Integer playlistid) {
        verificarPlaylist(playlistid);
        return new ResponseEntity<List<Reproducao>>(repo.findByPlaylistidOrderByDatahoraAscIdAsc(playlistid), HttpStatus.OK);
    }

    @GetMapping("/reproducao/total/{playlistid}")
    public ResponseEntity<Long> total(@PathVariable Integer playlistid) {
        verificarPlaylist(playlistid);
        return new ResponseEntity<Long>(repo.countByPlaylistid(playlistid), HttpStatus.OK);
    }

    private void verificarPlaylist(Integer playlistid) {
        if (!playlists.existsById(playlistid)) {
            throw new RecursoInexistenteException("Playlist não localizada");
        }
    }
}
