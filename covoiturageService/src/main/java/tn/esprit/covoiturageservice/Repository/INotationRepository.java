package tn.esprit.covoiturageservice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.covoiturageservice.Entity.Notation;

import java.util.List;

public interface INotationRepository extends JpaRepository<Notation, Long> {
    List<Notation> findByIdDonneur(long idDonneur);
    List<Notation> findByIdRecepteur(long idRecepteur);
}
