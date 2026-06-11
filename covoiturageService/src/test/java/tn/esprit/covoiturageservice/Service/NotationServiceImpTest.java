package tn.esprit.covoiturageservice.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.covoiturageservice.Entity.Notation;
import tn.esprit.covoiturageservice.Repository.INotationRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotationServiceImpTest {

    @Mock
    private INotationRepository notationRepository;

    @InjectMocks
    private NotationServiceImp service;

    private Notation notation;

    @BeforeEach
    void setUp() {
        notation = new Notation();
        notation.setId(1L);
        notation.setNotation(5);
        notation.setComment("Tres bon trajet");
        notation.setIdDonneur(10L);
        notation.setIdRecepteur(20L);
    }

    @Test
    @DisplayName("getNotationById renvoie la notation")
    void getNotationById_returns() {
        when(notationRepository.findById(1L)).thenReturn(Optional.of(notation));
        assertThat(service.getNotationById(1L)).isEqualTo(notation);
    }

    @Test
    @DisplayName("getNotationById renvoie null si introuvable")
    void getNotationById_returnsNull() {
        when(notationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(service.getNotationById(99L)).isNull();
    }

    @Test
    @DisplayName("getAllNotations renvoie tout")
    void getAllNotations_returnsAll() {
        when(notationRepository.findAll()).thenReturn(List.of(notation));
        assertThat(service.getAllNotations()).hasSize(1);
    }

    @Test
    @DisplayName("getNotationsByDonneur delegue au repository")
    void getNotationsByDonneur_delegates() {
        when(notationRepository.findByIdDonneur(10L)).thenReturn(List.of(notation));
        assertThat(service.getNotationsByDonneur(10L)).containsExactly(notation);
    }

    @Test
    @DisplayName("getNotationsByRecepteur delegue au repository")
    void getNotationsByRecepteur_delegates() {
        when(notationRepository.findByIdRecepteur(20L)).thenReturn(List.of(notation));
        assertThat(service.getNotationsByRecepteur(20L)).containsExactly(notation);
    }

    @Test
    @DisplayName("addNotation sauvegarde")
    void addNotation_saves() {
        when(notationRepository.save(notation)).thenReturn(notation);
        assertThat(service.addNotation(notation)).isEqualTo(notation);
        verify(notationRepository).save(notation);
    }

    @Test
    @DisplayName("updateNotation sauvegarde")
    void updateNotation_saves() {
        when(notationRepository.save(notation)).thenReturn(notation);
        assertThat(service.updateNotation(notation)).isEqualTo(notation);
    }

    @Test
    @DisplayName("deleteNotation delegue au repository")
    void deleteNotation_delegates() {
        service.deleteNotation(1L);
        verify(notationRepository).deleteById(1L);
    }
}
