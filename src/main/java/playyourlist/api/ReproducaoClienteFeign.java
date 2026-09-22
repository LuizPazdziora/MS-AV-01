package playyourlist.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import playyourlist.reproducoes.Reproducao;
import playyourlist.reproducoes.ReproducaoPedido;

@FeignClient(name = "reproducoes", url = "${servicos.url}")
public interface ReproducaoClienteFeign {
    @PostMapping("/statistic")
    Reproducao registrar(@RequestBody ReproducaoPedido pedido);
}
