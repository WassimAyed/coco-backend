package tn.esprit.collocationservice.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.collocationservice.Entity.collocOffre;
import tn.esprit.collocationservice.Entity.collocOffreFavorite;
import tn.esprit.collocationservice.Repository.collocOffreFavoriteRepo;
import tn.esprit.collocationservice.Repository.collocOffreRepo;
import tn.esprit.collocationservice.Exception.OfferNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("collocOffreFavoriteServiceImpl Unit Tests")
class CollocOffreFavoriteServiceImplTest {

    @Mock
    private collocOffreFavoriteRepo favoriteRepo;

    @Mock
    private collocOffreRepo offreRepo;

    @InjectMocks
    private collocOffreFavoriteServiceImpl service;

    private collocOffre sampleOffer;
    private collocOffreFavorite sampleFavorite;

    @BeforeEach
    void setUp() {
        sampleOffer = new collocOffre();
        sampleOffer.setId(1L);
        sampleOffer.setTitre("Studio Tunis");
        sampleOffer.setOwnerId(100L);

        sampleFavorite = new collocOffreFavorite();
        sampleFavorite.setId(10L);
        sampleFavorite.setUserId(200L);
        sampleFavorite.setOffre(sampleOffer);
    }

    // =========================================================================
    // GET FAVORITES BY USER
    // =========================================================================
    @Nested
    @DisplayName("getFavoritesByUser()")
    class GetFavoritesByUserTests {

        @Test
        @DisplayName("should return all favorites for a given user")
        void getFavoritesByUser_shouldReturnUserFavorites() {
            collocOffreFavorite fav2 = new collocOffreFavorite();
            fav2.setId(11L);
            fav2.setUserId(200L);
            collocOffre offer2 = new collocOffre();
            offer2.setId(2L);
            fav2.setOffre(offer2);

            when(favoriteRepo.findByUserId(200L)).thenReturn(List.of(sampleFavorite, fav2));

            List<collocOffreFavorite> result = service.getFavoritesByUser(200L);

            assertThat(result)
                    .hasSize(2)
                    .allSatisfy(f -> assertThat(f.getUserId()).isEqualTo(200L));
            verify(favoriteRepo).findByUserId(200L);
        }

        @Test
        @DisplayName("should return empty list when user has no favorites")
        void getFavoritesByUser_whenNone_shouldReturnEmpty() {
            when(favoriteRepo.findByUserId(999L)).thenReturn(Collections.emptyList());

            List<collocOffreFavorite> result = service.getFavoritesByUser(999L);

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // ADD FAVORITE
    // =========================================================================
    @Nested
    @DisplayName("addFavorite()")
    class AddFavoriteTests {

        @Test
        @DisplayName("should add favorite successfully when not already favorited")
        void addFavorite_whenNew_shouldSaveAndReturn() {
            Long userId = 300L;
            Long offreId = 1L;

            when(offreRepo.findById(offreId)).thenReturn(Optional.of(sampleOffer));
            when(favoriteRepo.findByUserIdAndOffreId(userId, offreId)).thenReturn(Optional.empty());
            when(favoriteRepo.save(any(collocOffreFavorite.class))).thenAnswer(invocation -> {
                collocOffreFavorite saved = invocation.getArgument(0);
                saved.setId(20L);
                return saved;
            });

            collocOffreFavorite result = service.addFavorite(userId, offreId);

            assertThat(result)
                    .isNotNull()
                    .extracting(
                            collocOffreFavorite::getId,
                            collocOffreFavorite::getUserId,
                            collocOffreFavorite::getOffre)
                    .containsExactly(20L, userId, sampleOffer);

            verify(offreRepo).findById(offreId);
            verify(favoriteRepo).findByUserIdAndOffreId(userId, offreId);
            verify(favoriteRepo).save(any(collocOffreFavorite.class));
        }

        @Test
        @DisplayName("should throw OfferNotFoundException when offer not found")
        void addFavorite_whenOfferNotFound_shouldThrow() {
            when(offreRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addFavorite(200L, 999L))
                    .isInstanceOf(OfferNotFoundException.class)
                    .hasMessageContaining("Offer not found");

            verify(favoriteRepo, never()).save(any());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when already favorited")
        void addFavorite_whenAlreadyFavorited_shouldThrow() {
            Long userId = 200L;
            Long offreId = 1L;

            when(offreRepo.findById(offreId)).thenReturn(Optional.of(sampleOffer));
            when(favoriteRepo.findByUserIdAndOffreId(userId, offreId))
                    .thenReturn(Optional.of(sampleFavorite));

            assertThatThrownBy(() -> service.addFavorite(userId, offreId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Already favorited");

            verify(favoriteRepo, never()).save(any());
        }
    }

    // =========================================================================
    // REMOVE FAVORITE
    // =========================================================================
    @Nested
    @DisplayName("removeFavorite()")
    class RemoveFavoriteTests {

        @Test
        @DisplayName("should call repository deleteByUserIdAndOffreId")
        void removeFavorite_shouldCallRepoDelete() {
            Long userId = 200L;
            Long offreId = 1L;

            doNothing().when(favoriteRepo).deleteByUserIdAndOffreId(userId, offreId);

            service.removeFavorite(userId, offreId);

            verify(favoriteRepo).deleteByUserIdAndOffreId(userId, offreId);
        }
    }
}
