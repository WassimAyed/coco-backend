package tn.esprit.eventservice.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tn.esprit.eventservice.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RepositoryTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private EventRatingRepository eventRatingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private EventGalleryRepository eventGalleryRepository;

    @Mock
    private ReactionRepository reactionRepository;

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private Category buildCategory(Long id, String name) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        c.setDescription(name + " description");
        return c;
    }

    private Event buildEvent(Long id, String name, Category category, EventStatus status) {
        Event e = new Event();
        e.setId(id);
        e.setName(name);
        e.setLocation("Tunis");
        e.setStartDate(LocalDateTime.now().plusDays(1));
        e.setEndDate(LocalDateTime.now().plusDays(2));
        e.setMaxCapacity(100);
        e.setCurrentParticipants(10);
        e.setStatus(status);
        e.setCategory(category);
        e.setUserId(1L);
        e.setPrice(BigDecimal.ZERO);
        e.setEventType(EventType.INDOOR);
        return e;
    }

    private Participant buildParticipant(Long id, String email, Event event) {
        Participant p = new Participant();
        p.setId(id);
        p.setFullName("Ali Ben Ali");
        p.setEmail(email);
        p.setPhone("12345678");
        p.setRegistrationDate(LocalDateTime.now());
        p.setEvent(event);
        return p;
    }

    // ─────────────────────────────────────────────
    // CategoryRepository
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CategoryRepository_findByNameIgnoreCase_shouldReturnCategory")
    void categoryRepository_findByNameIgnoreCase_shouldReturnCategory() {
        // Given
        Category cat = buildCategory(1L, "Sport");
        given(categoryRepository.findByNameIgnoreCase("sport")).willReturn(Optional.of(cat));

        // When
        Optional<Category> result = categoryRepository.findByNameIgnoreCase("sport");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Sport");
    }

    @Test
    @DisplayName("CategoryRepository_findByNameIgnoreCase_shouldReturnEmpty_whenNotFound")
    void categoryRepository_findByNameIgnoreCase_shouldReturnEmpty_whenNotFound() {
        // Given
        given(categoryRepository.findByNameIgnoreCase("Unknown")).willReturn(Optional.empty());

        // When
        Optional<Category> result = categoryRepository.findByNameIgnoreCase("Unknown");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("CategoryRepository_existsByNameIgnoreCase_shouldReturnTrue_whenExists")
    void categoryRepository_existsByNameIgnoreCase_shouldReturnTrue_whenExists() {
        // Given
        given(categoryRepository.existsByNameIgnoreCase("MUSIC")).willReturn(true);

        // When + Then
        assertThat(categoryRepository.existsByNameIgnoreCase("MUSIC")).isTrue();
    }

    @Test
    @DisplayName("CategoryRepository_existsByNameIgnoreCase_shouldReturnFalse_whenNotExists")
    void categoryRepository_existsByNameIgnoreCase_shouldReturnFalse_whenNotExists() {
        // Given
        given(categoryRepository.existsByNameIgnoreCase("Unknown")).willReturn(false);

        // When + Then
        assertThat(categoryRepository.existsByNameIgnoreCase("Unknown")).isFalse();
    }

    @Test
    @DisplayName("CategoryRepository_save_shouldPersistCategory")
    void categoryRepository_save_shouldPersistCategory() {
        // Given
        Category cat = buildCategory(1L, "Tech");
        given(categoryRepository.save(cat)).willReturn(cat);

        // When
        Category saved = categoryRepository.save(cat);

        // Then
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getName()).isEqualTo("Tech");
        verify(categoryRepository).save(cat);
    }

    @Test
    @DisplayName("CategoryRepository_findById_shouldReturnCategory_whenExists")
    void categoryRepository_findById_shouldReturnCategory_whenExists() {
        // Given
        Category cat = buildCategory(2L, "Music");
        given(categoryRepository.findById(2L)).willReturn(Optional.of(cat));

        // When
        Optional<Category> result = categoryRepository.findById(2L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Music");
    }

    // ─────────────────────────────────────────────
    // EventRepository
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("EventRepository_findByStatus_shouldReturnMatchingEvents")
    void eventRepository_findByStatus_shouldReturnMatchingEvents() {
        // Given
        Category cat = buildCategory(1L, "Tech");
        Event eventA = buildEvent(1L, "Event A", cat, EventStatus.APPROVED);
        given(eventRepository.findByStatus(EventStatus.APPROVED)).willReturn(List.of(eventA));

        // When
        List<Event> approved = eventRepository.findByStatus(EventStatus.APPROVED);

        // Then
        assertThat(approved).hasSize(1);
        assertThat(approved.get(0).getName()).isEqualTo("Event A");
        assertThat(approved.get(0).getStatus()).isEqualTo(EventStatus.APPROVED);
    }

    @Test
    @DisplayName("EventRepository_findByCategoryId_shouldReturnEventsForCategory")
    void eventRepository_findByCategoryId_shouldReturnEventsForCategory() {
        // Given
        Category cat = buildCategory(1L, "Art");
        Event event = buildEvent(1L, "Art Event", cat, EventStatus.APPROVED);
        given(eventRepository.findByCategoryId(1L)).willReturn(List.of(event));

        // When
        List<Event> result = eventRepository.findByCategoryId(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Art Event");
    }

    @Test
    @DisplayName("EventRepository_findByNameContainingIgnoreCase_shouldReturnMatchingEvents")
    void eventRepository_findByNameContainingIgnoreCase_shouldReturnMatchingEvents() {
        // Given
        Category cat = buildCategory(1L, "Food");
        Event event = buildEvent(1L, "Tunis Food Festival", cat, EventStatus.APPROVED);
        given(eventRepository.findByNameContainingIgnoreCase("festival")).willReturn(List.of(event));

        // When
        List<Event> result = eventRepository.findByNameContainingIgnoreCase("festival");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Tunis Food Festival");
    }

    @Test
    @DisplayName("EventRepository_findAvailableEvents_shouldReturnEventsWithCapacity")
    void eventRepository_findAvailableEvents_shouldReturnEventsWithCapacity() {
        // Given
        Category cat = buildCategory(1L, "Dance");
        Event available = buildEvent(1L, "Open Event", cat, EventStatus.APPROVED);
        given(eventRepository.findAvailableEvents()).willReturn(List.of(available));

        // When
        List<Event> result = eventRepository.findAvailableEvents();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Open Event");
    }

    @Test
    @DisplayName("EventRepository_findByUserId_shouldReturnPagedEvents")
    void eventRepository_findByUserId_shouldReturnPagedEvents() {
        // Given
        Category cat = buildCategory(1L, "Comedy");
        Event event = buildEvent(1L, "My Event", cat, EventStatus.APPROVED);
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Event> page = new PageImpl<>(List.of(event), pageable, 1);
        given(eventRepository.findByUserId(1L, pageable)).willReturn(page);

        // When
        Page<Event> result = eventRepository.findByUserId(1L, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("EventRepository_countAvailableEvents_shouldReturnCorrectCount")
    void eventRepository_countAvailableEvents_shouldReturnCorrectCount() {
        // Given
        given(eventRepository.countAvailableEvents()).willReturn(3L);

        // When
        long count = eventRepository.countAvailableEvents();

        // Then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("EventRepository_findByDateRange_shouldReturnEventsInRange")
    void eventRepository_findByDateRange_shouldReturnEventsInRange() {
        // Given
        Category cat = buildCategory(1L, "History");
        Event event = buildEvent(1L, "Future Event", cat, EventStatus.APPROVED);
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().plusDays(10);
        given(eventRepository.findByDateRange(from, to)).willReturn(List.of(event));

        // When
        List<Event> result = eventRepository.findByDateRange(from, to);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).isEqualTo("Future Event");
    }

    @Test
    @DisplayName("EventRepository_countByStatus_shouldReturnGroupedCounts")
    void eventRepository_countByStatus_shouldReturnGroupedCounts() {
        // Given
        given(eventRepository.countByStatus()).willReturn(
                List.of(new Object[]{EventStatus.APPROVED, 3L}, new Object[]{EventStatus.PENDING, 1L})
        );

        // When
        List<Object[]> result = eventRepository.countByStatus();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0)[0]).isEqualTo(EventStatus.APPROVED);
        assertThat(result.get(0)[1]).isEqualTo(3L);
    }

    // ─────────────────────────────────────────────
    // ParticipantRepository
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("ParticipantRepository_existsByEmailAndEventId_shouldReturnTrue_whenExists")
    void participantRepository_existsByEmailAndEventId_shouldReturnTrue_whenExists() {
        // Given
        given(participantRepository.existsByEmailAndEventId("ali@test.com", 1L)).willReturn(true);

        // When + Then
        assertThat(participantRepository.existsByEmailAndEventId("ali@test.com", 1L)).isTrue();
    }

    @Test
    @DisplayName("ParticipantRepository_existsByEmailAndEventId_shouldReturnFalse_whenNotExists")
    void participantRepository_existsByEmailAndEventId_shouldReturnFalse_whenNotExists() {
        // Given
        given(participantRepository.existsByEmailAndEventId("notfound@test.com", 1L)).willReturn(false);

        // When + Then
        assertThat(participantRepository.existsByEmailAndEventId("notfound@test.com", 1L)).isFalse();
    }

    @Test
    @DisplayName("ParticipantRepository_findByEventId_shouldReturnParticipants")
    void participantRepository_findByEventId_shouldReturnParticipants() {
        // Given
        Category cat = buildCategory(1L, "Cinema");
        Event event = buildEvent(1L, "Film Night", cat, EventStatus.APPROVED);
        Participant p = buildParticipant(1L, "sara@test.com", event);
        given(participantRepository.findByEventId(1L)).willReturn(List.of(p));

        // When
        List<Participant> result = participantRepository.findByEventId(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("sara@test.com");
    }

    @Test
    @DisplayName("ParticipantRepository_countByEventId_shouldReturnCorrectCount")
    void participantRepository_countByEventId_shouldReturnCorrectCount() {
        // Given
        given(participantRepository.countByEventId(1L)).willReturn(5L);

        // When
        long count = participantRepository.countByEventId(1L);

        // Then
        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("ParticipantRepository_findDistinctEventIdsByEmail_shouldReturnEventIds")
    void participantRepository_findDistinctEventIdsByEmail_shouldReturnEventIds() {
        // Given
        given(participantRepository.findDistinctEventIdsByEmail("ali@test.com"))
                .willReturn(List.of(1L, 2L, 3L));

        // When
        List<Long> result = participantRepository.findDistinctEventIdsByEmail("ali@test.com");

        // Then
        assertThat(result).containsExactly(1L, 2L, 3L);
    }

    // ─────────────────────────────────────────────
    // EventRatingRepository
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("EventRatingRepository_findByEventIdAndUserId_shouldReturnRating")
    void eventRatingRepository_findByEventIdAndUserId_shouldReturnRating() {
        // Given
        EventRating rating = new EventRating();
        rating.setId(1L);
        rating.setUserId(1L);
        rating.setRating(5);
        given(eventRatingRepository.findByEventIdAndUserId(1L, 1L)).willReturn(Optional.of(rating));

        // When
        Optional<EventRating> result = eventRatingRepository.findByEventIdAndUserId(1L, 1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("EventRatingRepository_existsByEventIdAndUserId_shouldReturnTrue")
    void eventRatingRepository_existsByEventIdAndUserId_shouldReturnTrue() {
        // Given
        given(eventRatingRepository.existsByEventIdAndUserId(1L, 1L)).willReturn(true);

        // When + Then
        assertThat(eventRatingRepository.existsByEventIdAndUserId(1L, 1L)).isTrue();
    }

    @Test
    @DisplayName("EventRatingRepository_findAverageRatingByEventId_shouldReturnAverage")
    void eventRatingRepository_findAverageRatingByEventId_shouldReturnAverage() {
        // Given
        given(eventRatingRepository.findAverageRatingByEventId(1L)).willReturn(3.5);

        // When
        Double avg = eventRatingRepository.findAverageRatingByEventId(1L);

        // Then
        assertThat(avg).isEqualTo(3.5);
    }

    @Test
    @DisplayName("EventRatingRepository_countByEventId_shouldReturnTotal")
    void eventRatingRepository_countByEventId_shouldReturnTotal() {
        // Given
        given(eventRatingRepository.countByEventId(1L)).willReturn(4L);

        // When
        Long count = eventRatingRepository.countByEventId(1L);

        // Then
        assertThat(count).isEqualTo(4L);
    }

    // ─────────────────────────────────────────────
    // CommentRepository
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CommentRepository_findByEventIdOrderByCreatedAtDesc_shouldReturnComments")
    void commentRepository_findByEventIdOrderByCreatedAtDesc_shouldReturnComments() {
        // Given
        Comment c = new Comment();
        c.setId(1L);
        c.setContent("Super événement !");
        c.setAuthorEmail("user@test.com");
        c.setCreatedAt(LocalDateTime.now());
        given(commentRepository.findByEventIdOrderByCreatedAtDesc(1L)).willReturn(List.of(c));

        // When
        List<Comment> result = commentRepository.findByEventIdOrderByCreatedAtDesc(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Super événement !");
    }

    @Test
    @DisplayName("CommentRepository_countByEventId_shouldReturnCount")
    void commentRepository_countByEventId_shouldReturnCount() {
        // Given
        given(commentRepository.countByEventId(1L)).willReturn(3L);

        // When
        long count = commentRepository.countByEventId(1L);

        // Then
        assertThat(count).isEqualTo(3L);
    }

    // ─────────────────────────────────────────────
    // EventGalleryRepository
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("EventGalleryRepository_findByEventId_shouldReturnImages")
    void eventGalleryRepository_findByEventId_shouldReturnImages() {
        // Given
        EventGallery img = new EventGallery();
        img.setId(1L);
        img.setImageUrl("http://img.jpg");
        img.setCaption("Photo 1");
        given(eventGalleryRepository.findByEventId(1L)).willReturn(List.of(img));

        // When
        List<EventGallery> result = eventGalleryRepository.findByEventId(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCaption()).isEqualTo("Photo 1");
        assertThat(result.get(0).getImageUrl()).isEqualTo("http://img.jpg");
    }

    @Test
    @DisplayName("EventGalleryRepository_findByEventId_shouldReturnEmpty_whenNoImages")
    void eventGalleryRepository_findByEventId_shouldReturnEmpty_whenNoImages() {
        // Given
        given(eventGalleryRepository.findByEventId(99L)).willReturn(List.of());

        // When
        List<EventGallery> result = eventGalleryRepository.findByEventId(99L);

        // Then
        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────
    // ReactionRepository
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("ReactionRepository_existsByEventIdAndAuthorEmail_shouldReturnTrue")
    void reactionRepository_existsByEventIdAndAuthorEmail_shouldReturnTrue() {
        // Given
        given(reactionRepository.existsByEventIdAndAuthorEmail(1L, "reactor@test.com")).willReturn(true);

        // When + Then
        assertThat(reactionRepository.existsByEventIdAndAuthorEmail(1L, "reactor@test.com")).isTrue();
    }

    @Test
    @DisplayName("ReactionRepository_existsByEventIdAndAuthorEmail_shouldReturnFalse_whenNotExists")
    void reactionRepository_existsByEventIdAndAuthorEmail_shouldReturnFalse_whenNotExists() {
        // Given
        given(reactionRepository.existsByEventIdAndAuthorEmail(1L, "nobody@test.com")).willReturn(false);

        // When + Then
        assertThat(reactionRepository.existsByEventIdAndAuthorEmail(1L, "nobody@test.com")).isFalse();
    }

    @Test
    @DisplayName("ReactionRepository_countByEventId_shouldReturnCount")
    void reactionRepository_countByEventId_shouldReturnCount() {
        // Given
        given(reactionRepository.countByEventId(1L)).willReturn(7L);

        // When
        long count = reactionRepository.countByEventId(1L);

        // Then
        assertThat(count).isEqualTo(7L);
    }

    @Test
    @DisplayName("ReactionRepository_findByEventId_shouldReturnReactions")
    void reactionRepository_findByEventId_shouldReturnReactions() {
        // Given
        Reaction r = new Reaction();
        r.setId(1L);
        r.setAuthorEmail("user@test.com");
        r.setType(ReactionType.LIKE);
        given(reactionRepository.findByEventId(1L)).willReturn(List.of(r));

        // When
        List<Reaction> result = reactionRepository.findByEventId(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(ReactionType.LIKE);
    }
}