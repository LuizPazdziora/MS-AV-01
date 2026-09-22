package playyourlist.playlists;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface PlaylistMusicaRepository extends CrudRepository<PlaylistMusica, Integer> {
    List<PlaylistMusica> findByPlaylistidOrderByIdAsc(Integer playlistid);
    Optional<PlaylistMusica> findByPlaylistidAndMusicaid(Integer playlistid, Integer musicaid);
}
