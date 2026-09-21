package aula.microservicos.playyourlist.musicas;

import org.springframework.data.repository.CrudRepository;

public interface MusicaRepository extends CrudRepository<Musica, Integer> {
}
