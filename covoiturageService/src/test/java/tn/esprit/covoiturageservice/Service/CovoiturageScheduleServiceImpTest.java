package tn.esprit.covoiturageservice.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.covoiturageservice.Entity.Covoiturage;
import tn.esprit.covoiturageservice.Entity.CovoiturageSchedule;
import tn.esprit.covoiturageservice.Entity.CovoiturageSchedule.Frequency;
import tn.esprit.covoiturageservice.Repository.ICovoiturageRepository;
import tn.esprit.covoiturageservice.Repository.ICovoiturageScheduleRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CovoiturageScheduleServiceImpTest {

    @Mock
    private ICovoiturageScheduleRepository scheduleRepository;
    @Mock
    private ICovoiturageRepository covoiturageRepository;

    @InjectMocks
    private CovoiturageScheduleServiceImp service;

    private CovoiturageSchedule schedule;

    @BeforeEach
    void setUp() {
        schedule = new CovoiturageSchedule();
        schedule.setId(1L);
        schedule.setPointDepart("Tunis");
        schedule.setPointArrivee("Sousse");
        schedule.setNombrePlaces(4);
        schedule.setPrixParPassager(20.0);
        schedule.setIdDriver(10L);
        schedule.setVehicleId(5L);
        schedule.setHeureDepart(LocalTime.of(8, 0));
        schedule.setFrequency(Frequency.DAILY);
        schedule.setActive(true);
    }

    @Test
    @DisplayName("add definit createdAt et startDate par defaut puis sauvegarde")
    void add_setsDefaults() {
        schedule.setStartDate(null);
        when(scheduleRepository.save(any(CovoiturageSchedule.class))).thenAnswer(inv -> inv.getArgument(0));

        CovoiturageSchedule saved = service.add(schedule);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getStartDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("update sauvegarde le schedule")
    void update_saves() {
        when(scheduleRepository.save(schedule)).thenReturn(schedule);
        assertThat(service.update(schedule)).isEqualTo(schedule);
    }

    @Test
    @DisplayName("getById renvoie le schedule")
    void getById_returns() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        assertThat(service.getById(1L)).isEqualTo(schedule);
    }

    @Test
    @DisplayName("getById renvoie null quand introuvable")
    void getById_returnsNull() {
        when(scheduleRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(service.getById(99L)).isNull();
    }

    @Test
    @DisplayName("getAll renvoie tous les schedules")
    void getAll_returnsAll() {
        when(scheduleRepository.findAll()).thenReturn(List.of(schedule));
        assertThat(service.getAll()).hasSize(1);
    }

    @Test
    @DisplayName("getByDriver delegue au repository")
    void getByDriver_delegates() {
        when(scheduleRepository.findByIdDriver(10L)).thenReturn(List.of(schedule));
        assertThat(service.getByDriver(10L)).containsExactly(schedule);
    }

    @Test
    @DisplayName("delete delegue au repository")
    void delete_delegates() {
        service.delete(1L);
        verify(scheduleRepository).deleteById(1L);
    }

    @Test
    @DisplayName("toggleActive bascule active=true vers false")
    void toggleActive_flipsActive() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(schedule)).thenReturn(schedule);

        CovoiturageSchedule result = service.toggleActive(1L);

        assertThat(result.isActive()).isFalse();
    }

    @Test
    @DisplayName("toggleActive renvoie null quand schedule introuvable")
    void toggleActive_returnsNullWhenMissing() {
        when(scheduleRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(service.toggleActive(99L)).isNull();
    }

    @Test
    @DisplayName("generateDueCovoiturages cree un trajet pour un schedule DAILY")
    void generateDueCovoiturages_createsTripForDaily() {
        schedule.setLastGeneratedDate(null);
        when(scheduleRepository.findByActiveTrue()).thenReturn(List.of(schedule));

        int generated = service.generateDueCovoiturages();

        assertThat(generated).isEqualTo(1);
        verify(covoiturageRepository).save(any(Covoiturage.class));
        verify(scheduleRepository).save(schedule);
        assertThat(schedule.getLastGeneratedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("generateDueCovoiturages ignore un schedule deja genere aujourd'hui")
    void generateDueCovoiturages_skipsIfAlreadyGeneratedToday() {
        schedule.setLastGeneratedDate(LocalDate.now());
        when(scheduleRepository.findByActiveTrue()).thenReturn(List.of(schedule));

        int generated = service.generateDueCovoiturages();

        assertThat(generated).isZero();
        verify(covoiturageRepository, never()).save(any(Covoiturage.class));
    }

    @Test
    @DisplayName("generateDueCovoiturages ignore un schedule hors plage de dates")
    void generateDueCovoiturages_skipsOutOfRange() {
        schedule.setStartDate(LocalDate.now().plusDays(5));
        when(scheduleRepository.findByActiveTrue()).thenReturn(List.of(schedule));

        int generated = service.generateDueCovoiturages();

        assertThat(generated).isZero();
    }

    @Test
    @DisplayName("generateDueCovoiturages WEEKLY genere uniquement les bons jours")
    void generateDueCovoiturages_weeklyMatchesDay() {
        schedule.setFrequency(Frequency.WEEKLY);
        // ne contient PAS le jour actuel
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        DayOfWeek other = today == DayOfWeek.MONDAY ? DayOfWeek.TUESDAY : DayOfWeek.MONDAY;
        schedule.setDaysOfWeek(toShortCode(other));

        when(scheduleRepository.findByActiveTrue()).thenReturn(List.of(schedule));

        int generated = service.generateDueCovoiturages();

        assertThat(generated).isZero();
    }

    private String toShortCode(DayOfWeek d) {
        return switch (d) {
            case MONDAY -> "MON";
            case TUESDAY -> "TUE";
            case WEDNESDAY -> "WED";
            case THURSDAY -> "THU";
            case FRIDAY -> "FRI";
            case SATURDAY -> "SAT";
            case SUNDAY -> "SUN";
        };
    }
}
