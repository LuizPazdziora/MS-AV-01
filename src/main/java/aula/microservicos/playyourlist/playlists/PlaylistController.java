package aula.microservicos.playyourlist.playlists;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/playlists")
public class PlaylistController {
    private final PlaylistRepository repo;
    private final PlaylistService servico;

    public PlaylistController(PlaylistRepository repo, PlaylistService servico) {
        this.repo = repo;
        this.servico = servico;
    }

    @GetMapping
    public ResponseEntity<Iterable<Playlist>> listar() {
        return new ResponseEntity<Iterable<Playlist>>(repo.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{playlistid}")
    public ResponseEntity<Playlist> buscar(@PathVariable Integer playlistid) {
        return new ResponseEntity<Playlist>(servico.buscar(playlistid), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Playlist> cadastrar(@Valid @RequestBody Playlist playlist) {
        playlist.id = null;
        return new ResponseEntity<Playlist>(repo.save(playlist), HttpStatus.CREATED);
    }

    @PutMapping("/{playlistid}")
    public ResponseEntity<Playlist> atualizar(@PathVariable Integer playlistid, @Valid @RequestBody Playlist playlist) {
        servico.buscar(playlistid);
        playlist.id = playlistid;
        return new ResponseEntity<Playlist>(repo.save(playlist), HttpStatus.OK);
    }

    @DeleteMapping("/{playlistid}")
    public ResponseEntity<Void> remover(@PathVariable Integer playlistid) {
        servico.remover(playlistid);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{playlistid}/musicas/{musicaId}")
    public ResponseEntity<PlaylistMusica> adicionar(@PathVariable Integer playlistid, @PathVariable Integer musicaId) {
        return new ResponseEntity<PlaylistMusica>(servico.adicionar(playlistid, musicaId), HttpStatus.CREATED);
    }

    @DeleteMapping("/{playlistid}/musicas/{musicaId}")
    public ResponseEntity<Void> removerMusica(@PathVariable Integer playlistid, @PathVariable Integer musicaId) {
        servico.removerMusica(playlistid, musicaId);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{playlistid}/musicas")
    public ResponseEntity<List<Integer>> listarMusicas(@PathVariable Integer playlistid) {
        return new ResponseEntity<List<Integer>>(servico.listarMusicas(playlistid), HttpStatus.OK);
    }
}
