package aula.microservicos.playyourlist.playlists;

import jakarta.persistence.*;

@Entity
@Table(name = "playlist_musicas", uniqueConstraints = @UniqueConstraint(columnNames = {"playlistid", "musicaid"}))
public class PlaylistMusica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    @Column(nullable = false)
    public Integer playlistid;
    @Column(nullable = false)
    public Integer musicaid;
}
