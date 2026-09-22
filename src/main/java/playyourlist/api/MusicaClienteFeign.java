package playyourlist.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import playyourlist.musicas.Musica;

@FeignClient(name = "musicas", url = "${servicos.url}")
public interface MusicaClienteFeign {
    @GetMapping("/musicas/{id}")
    Musica buscar(@PathVariable("id") Integer id);
}
