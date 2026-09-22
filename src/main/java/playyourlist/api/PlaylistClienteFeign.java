package playyourlist.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import playyourlist.playlists.Playlist;
import playyourlist.playlists.PlaylistMusica;

@FeignClient(name = "playlists", url = "${servicos.url}")
public interface PlaylistClienteFeign {
    @GetMapping("/playlists/{playlistid}")
    Playlist buscar(@PathVariable("playlistid") Integer playlistid);

    @PostMapping("/playlists/{playlistid}/musicas/{musicaId}")
    PlaylistMusica adicionar(@PathVariable("playlistid") Integer playlistid, @PathVariable("musicaId") Integer musicaId);
}
