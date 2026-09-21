package aula.microservicos.playyourlist.playlists;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import aula.microservicos.playyourlist.musicas.MusicaRepository;
import aula.microservicos.playyourlist.excecoes.*;

@Service
public class PlaylistService {
    private final PlaylistRepository playlists;
    private final MusicaRepository musicas;
    private final PlaylistMusicaRepository vinculos;

    public PlaylistService(PlaylistRepository playlists, MusicaRepository musicas, PlaylistMusicaRepository vinculos) {
        this.playlists = playlists;
        this.musicas = musicas;
        this.vinculos = vinculos;
    }

    public Playlist buscar(Integer playlistid) {
        Optional<Playlist> resultado = playlists.findById(playlistid);
        if (resultado.isPresent()) {
            return resultado.get();
        } else {
            throw new RecursoInexistenteException("Playlist não localizada");
        }
    }

    @Transactional
    public PlaylistMusica adicionar(Integer playlistid, Integer musicaId) {
        buscar(playlistid);
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
        return vinculos.save(vinculo);
    }

    @Transactional
    public void removerMusica(Integer playlistid, Integer musicaId) {
        buscar(playlistid);
        Optional<PlaylistMusica> resultado = vinculos.findByPlaylistidAndMusicaid(playlistid, musicaId);
        if (resultado.isPresent()) {
            vinculos.delete(resultado.get());
        } else {
            throw new RecursoInexistenteException("Música não associada à playlist");
        }
    }

    public List<Integer> listarMusicas(Integer playlistid) {
        buscar(playlistid);
        List<Integer> ids = new ArrayList<Integer>();
        for (PlaylistMusica vinculo : vinculos.findByPlaylistidOrderByIdAsc(playlistid)) {
            ids.add(vinculo.musicaid);
        }
        return ids;
    }

    @Transactional
    public void remover(Integer playlistid) {
        Playlist playlist = buscar(playlistid);
        // As chaves estrangeiras removem vínculos e reproduções, preservando as músicas.
        playlists.delete(playlist);
    }
}
