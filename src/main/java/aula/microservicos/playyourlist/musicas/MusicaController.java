package aula.microservicos.playyourlist.musicas;

import java.util.Optional;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import aula.microservicos.playyourlist.excecoes.RecursoInexistenteException;

@RestController
@RequestMapping("/musicas")
public class MusicaController {
    private final MusicaRepository repo;

    public MusicaController(MusicaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<Iterable<Musica>> listar() {
        return new ResponseEntity<Iterable<Musica>>(repo.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Musica> buscar(@PathVariable Integer id) {
        Optional<Musica> resultado = repo.findById(id);
        if (resultado.isPresent()) {
            return new ResponseEntity<Musica>(resultado.get(), HttpStatus.OK);
        } else {
            throw new RecursoInexistenteException("Música não localizada");
        }
    }

    @PostMapping
    public ResponseEntity<Musica> cadastrar(@Valid @RequestBody Musica musica) {
        musica.id = null;
        return new ResponseEntity<Musica>(repo.save(musica), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Musica> atualizar(@PathVariable Integer id, @Valid @RequestBody Musica musica) {
        if (repo.existsById(id)) {
            musica.id = id;
            return new ResponseEntity<Musica>(repo.save(musica), HttpStatus.OK);
        } else {
            throw new RecursoInexistenteException("Música não localizada");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Integer id) {
        if (repo.existsById(id)) {
            // O banco remove somente os vínculos dessa música nas playlists.
            repo.deleteById(id);
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        } else {
            throw new RecursoInexistenteException("Música não localizada");
        }
    }
}
