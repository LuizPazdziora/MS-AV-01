package playyourlist.reproducoes;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ReproducaoPedido {
    @NotNull(message = "Playlist é obrigatória")
    @Positive(message = "Playlist deve ter um ID positivo")
    public Integer playlistid;
}
