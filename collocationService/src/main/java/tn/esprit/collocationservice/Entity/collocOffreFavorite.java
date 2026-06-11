package tn.esprit.collocationservice.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class collocOffreFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // ID of the user

    @ManyToOne
    @JoinColumn(name = "offre_id")
    private collocOffre offre;
}