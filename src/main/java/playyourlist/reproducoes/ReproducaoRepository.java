package playyourlist.reproducoes;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ReproducaoRepository extends CrudRepository<Reproducao, Integer> {
    List<Reproducao> findByPlaylistidOrderByDatahoraAscIdAsc(Integer playlistid);
    long countByPlaylistid(Integer playlistid);
}
