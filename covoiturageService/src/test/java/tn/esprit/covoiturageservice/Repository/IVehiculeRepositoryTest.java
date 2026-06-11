package tn.esprit.covoiturageservice.Repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import tn.esprit.covoiturageservice.Entity.Vehicule;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
class IVehiculeRepositoryTest {

    @Autowired
    private IVehiculeRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("findByIdUtilisateur renvoie les vehicules d'un utilisateur")
    void findByIdUtilisateur_returnsForUser() {
        repository.save(build(10L, "Peugeot", "123 TU 4567"));
        repository.save(build(10L, "Renault", "456 TU 7890"));
        repository.save(build(20L, "BMW", "789 TU 1234"));

        List<Vehicule> result = repository.findByIdUtilisateur(10L);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByIdUtilisateur renvoie liste vide quand inexistant")
    void findByIdUtilisateur_emptyWhenUnknown() {
        assertThat(repository.findByIdUtilisateur(999L)).isEmpty();
    }

    @Test
    @DisplayName("save persiste un vehicule et lui assigne un id")
    void save_persistsVehicule() {
        Vehicule v = build(10L, "Toyota", "111 TU 2222");
        Vehicule saved = repository.save(v);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    private Vehicule build(long idUtilisateur, String marque, String immat) {
        Vehicule v = new Vehicule();
        v.setIdUtilisateur(idUtilisateur);
        v.setMarque(marque);
        v.setImmatriculation(immat);
        v.setCouleur("Noir");
        v.setCapacite(4);
        return v;
    }
}
