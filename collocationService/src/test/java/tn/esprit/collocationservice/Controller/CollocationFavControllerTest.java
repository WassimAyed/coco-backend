package tn.esprit.collocationservice.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tn.esprit.collocationservice.Entity.collocOffre;
import tn.esprit.collocationservice.Entity.collocOffreFavorite;
import tn.esprit.collocationservice.Service.collocOffreFavoriteService;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CollocationFavController Unit Tests")
class CollocationFavControllerTest {

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleServerError(RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private MockMvc mockMvc;

    @Mock private collocOffreFavoriteService favoriteService;
    @InjectMocks private CollocationFavController controller;

    private collocOffre sampleOffer;
    private collocOffreFavorite sampleFavorite;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new TestExceptionHandler())
                .build();

        sampleOffer = new collocOffre();
        sampleOffer.setId(1L);
        sampleOffer.setTitre("Studio Tunis");

        sampleFavorite = new collocOffreFavorite();
        sampleFavorite.setId(10L);
        sampleFavorite.setUserId(200L);
        sampleFavorite.setOffre(sampleOffer);
    }

    @Nested
    @DisplayName("GET /collocation/favorites/{userId}")
    class GetFavoritesTests {
        @Test
        @DisplayName("should return 200 with list of favorites for user")
        void getFavorites_shouldReturn200() throws Exception {
            when(favoriteService.getFavoritesByUser(200L)).thenReturn(List.of(sampleFavorite));

            mockMvc.perform(get("/collocation/favorites/200"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].userId").value(200));
        }

        @Test
        @DisplayName("should return 200 with empty list when no favorites")
        void getFavorites_whenNone_shouldReturnEmpty() throws Exception {
            when(favoriteService.getFavoritesByUser(999L)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/collocation/favorites/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("POST /collocation/favorites/{userId}/{offreId}")
    class AddFavoriteTests {
        @Test
        @DisplayName("should return 200 with saved favorite")
        void addFavorite_shouldReturn200() throws Exception {
            when(favoriteService.addFavorite(200L, 1L)).thenReturn(sampleFavorite);

            mockMvc.perform(post("/collocation/favorites/200/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10));
        }

        @Test
        @DisplayName("should return 500 when offer not found")
        void addFavorite_whenOfferNotFound_shouldReturn500() throws Exception {
            when(favoriteService.addFavorite(200L, 999L))
                    .thenThrow(new RuntimeException("Offer not found"));

            mockMvc.perform(post("/collocation/favorites/200/999"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("DELETE /collocation/favorites/{userId}/{offreId}")
    class RemoveFavoriteTests {
        @Test
        @DisplayName("should return 200 with success message")
        void removeFavorite_shouldReturn200() throws Exception {
            doNothing().when(favoriteService).removeFavorite(200L, 1L);

            mockMvc.perform(delete("/collocation/favorites/200/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Favorite removed successfully"));
        }

        @Test
        @DisplayName("should return 500 when removal fails")
        void removeFavorite_whenFails_shouldReturn500() throws Exception {
            doThrow(new RuntimeException("DB error")).when(favoriteService).removeFavorite(200L, 999L);

            mockMvc.perform(delete("/collocation/favorites/200/999"))
                    .andExpect(status().isInternalServerError());
        }
    }
}
