package playyourlist.musicas;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "musicas")
public class Musica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 150, message = "Título deve ter no máximo 150 caracteres")
    @Column(nullable = false, length = 150)
    public String titulo;

    @NotBlank(message = "Artista é obrigatório")
    @Size(max = 150, message = "Artista deve ter no máximo 150 caracteres")
    @Column(nullable = false, length = 150)
    public String artista;

    @Size(max = 150, message = "Álbum deve ter no máximo 150 caracteres")
    @Column(length = 150)
    public String album;

    @NotNull(message = "Duração é obrigatória")
    @Positive(message = "Duração deve ser maior que zero")
    @Column(nullable = false)
    public Integer duracao;

    @Size(max = 50, message = "Gênero deve ter no máximo 50 caracteres")
    @Column(length = 50)
    public String genero;
}
