package tn.esprit.covoiturageservice.Repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import tn.esprit.covoiturageservice.Entity.Notation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
class INotationRepositoryTest {

    @Autowired
    private INotationRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("findByIdDonneur renvoie les notations donnees par un utilisateur")
    void findByIdDonneur_returnsForGiver() {
        repository.save(build(10L, 20L, 5));
        repository.save(build(10L, 30L, 4));
        repository.save(build(20L, 30L, 3));

        List<Notation> result = repository.findByIdDonneur(10L);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByIdRecepteur renvoie les notations recues par un utilisateur")
    void findByIdRecepteur_returnsForReceiver() {
        repository.save(build(10L, 20L, 5));
        repository.save(build(30L, 20L, 4));
        repository.save(build(40L, 50L, 3));

        List<Notation> result = repository.findByIdRecepteur(20L);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByIdRecepteur renvoie liste vide pour utilisateur sans note")
    void findByIdRecepteur_emptyForUnknown() {
        assertThat(repository.findByIdRecepteur(999L)).isEmpty();
    }

    private Notation build(long idDonneur, long idRecepteur, int note) {
        Notation n = new Notation();
        n.setIdDonneur(idDonneur);
        n.setIdRecepteur(idRecepteur);
        n.setNotation(note);
        n.setComment("Test");
        return n;
    }
}
