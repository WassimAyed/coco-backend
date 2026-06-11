package tn.esprit.covoiturageservice.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Notation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private  int notation;
    private String comment;
    private long idDonneur;
    private long idRecepteur;
}
