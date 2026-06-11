package tn.esprit.collocationservice.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class collocOffreImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    private String url;

    @ManyToOne
    @JoinColumn(name = "offre_id")
    @JsonBackReference
    private collocOffre offre;

}
