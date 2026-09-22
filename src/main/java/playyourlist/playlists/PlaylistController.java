package playyourlist.playlists;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import playyourlist.excecoes.ConflitoException;
import playyourlist.excecoes.RecursoInexistenteException;
import playyourlist.musicas.MusicaRepository;

@RestController
@RequestMapping("/playlists")
public class PlaylistController {
    private final PlaylistRepository repo;
    private final PlaylistMusicaRepository vinculos;
    private final MusicaRepository musicas;

    public PlaylistController(PlaylistRepository repo, PlaylistMusicaRepository vinculos, MusicaRepository musicas) {
        this.repo = repo;
        this.vinculos = vinculos;
        this.musicas = musicas;
    }

    @GetMapping
    public ResponseEntity<Iterable<Playlist>> listar() {
        return new ResponseEntity<Iterable<Playlist>>(repo.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{playlistid}")
    public ResponseEntity<Playlist> buscar(@PathVariable Integer playlistid) {
        Optional<Playlist> resultado = repo.findById(playlistid);
        if (resultado.isPresent()) {
            return new ResponseEntity<Playlist>(resultado.get(), HttpStatus.OK);
        } else {
            throw new RecursoInexistenteException("Playlist não localizada");
        }
    }

    @PostMapping
    public ResponseEntity<Playlist> cadastrar(@Valid @RequestBody Playlist playlist) {
        playlist.id = null;
        return new ResponseEntity<Playlist>(repo.save(playlist), HttpStatus.CREATED);
    }

    @PutMapping("/{playlistid}")
    public ResponseEntity<Playlist> atualizar(@PathVariable Integer playlistid, @Valid @RequestBody Playlist playlist) {
        verificarPlaylist(playlistid);
        playlist.id = playlistid;
        return new ResponseEntity<Playlist>(repo.save(playlist), HttpStatus.OK);
    }

    @DeleteMapping("/{playlistid}")
    public ResponseEntity<Void> remover(@PathVariable Integer playlistid) {
        verificarPlaylist(playlistid);
        // O banco remove os vínculos da playlist com as músicas.
        repo.deleteById(playlistid);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{playlistid}/musicas/{musicaId}")
    public ResponseEntity<PlaylistMusica> adicionar(@PathVariable Integer playlistid, @PathVariable Integer musicaId) {
        verificarPlaylist(playlistid);
        if (!musicas.existsById(musicaId)) {
            throw new RecursoInexistenteException("Música não localizada");
        }
        Optional<PlaylistMusica> existente = vinculos.findByPlaylistidAndMusicaid(playlistid, musicaId);
        if (existente.isPresent()) {
            throw new ConflitoException("Música já associada à playlist");
        }
        PlaylistMusica vinculo = new PlaylistMusica();
        vinculo.playlistid = playlistid;
        vinculo.musicaid = musicaId;
        return new ResponseEntity<PlaylistMusica>(vinculos.save(vinculo), HttpStatus.CREATED);
    }

    @DeleteMapping("/{playlistid}/musicas/{musicaId}")
    public ResponseEntity<Void> removerMusica(@PathVariable Integer playlistid, @PathVariable Integer musicaId) {
        verificarPlaylist(playlistid);
        Optional<PlaylistMusica> resultado = vinculos.findByPlaylistidAndMusicaid(playlistid, musicaId);
        if (resultado.isPresent()) {
            vinculos.delete(resultado.get());
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        } else {
            throw new RecursoInexistenteException("Música não associada à playlist");
        }
    }

    @GetMapping("/{playlistid}/musicas")
    public ResponseEntity<List<Integer>> listarMusicas(@PathVariable Integer playlistid) {
        verificarPlaylist(playlistid);
        List<Integer> ids = new ArrayList<Integer>();
        for (PlaylistMusica vinculo : vinculos.findByPlaylistidOrderByIdAsc(playlistid)) {
            ids.add(vinculo.musicaid);
        }
        return new ResponseEntity<List<Integer>>(ids, HttpStatus.OK);
    }

    private void verificarPlaylist(Integer playlistid) {
        if (!repo.existsById(playlistid)) {
            throw new RecursoInexistenteException("Playlist não localizada");
        }
    }
}
