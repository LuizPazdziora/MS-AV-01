package aula.microservicos.playyourlist.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import aula.microservicos.playyourlist.reproducoes.Reproducao;
import aula.microservicos.playyourlist.reproducoes.ReproducaoPedido;

@FeignClient(name = "reproducoes", url = "${servicos.url}")
public interface ReproducaoClienteFeign {
    @PostMapping("/statistic")
    Reproducao registrar(@RequestBody ReproducaoPedido pedido);
}
