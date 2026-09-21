package aula.microservicos.playyourlist.reproducoes;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "reproducoes")
public class Reproducao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    @Column(nullable = false)
    public Integer playlistid;
    @Column(name = "datahora", nullable = false)
    public LocalDateTime datahora;
}
