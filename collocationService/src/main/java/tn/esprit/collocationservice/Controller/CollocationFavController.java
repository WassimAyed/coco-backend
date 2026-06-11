package tn.esprit.collocationservice.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.collocationservice.Entity.collocOffreFavorite;
import tn.esprit.collocationservice.Service.collocOffreFavoriteService;

import java.util.List;

@RestController
@RequestMapping("/collocation/favorites")
@RequiredArgsConstructor
public class CollocationFavController {
    private final collocOffreFavoriteService favoriteService;

    // Get all favorites of a user
    @GetMapping("/{userId}")
    public List<collocOffreFavorite> getFavorites(@PathVariable Long userId) {
        return favoriteService.getFavoritesByUser(userId);
    }

    // Add a favorite
    @PostMapping("/{userId}/{offreId}")
    public collocOffreFavorite addFavorite(@PathVariable Long userId, @PathVariable Long offreId) {
        return favoriteService.addFavorite(userId, offreId);
    }

    // Remove a favorite
    @DeleteMapping("/{userId}/{offreId}")
    public ResponseEntity<String> remove(
            @PathVariable Long userId,
            @PathVariable Long offreId) {

        favoriteService.removeFavorite(userId, offreId);
        return ResponseEntity.ok("Favorite removed successfully");
    }
}
