package tn.esprit.collocationservice.Service;

import tn.esprit.collocationservice.Entity.collocOffreFavorite;
import java.util.List;

public interface collocOffreFavoriteService {
    List<collocOffreFavorite> getFavoritesByUser(Long userId);
    collocOffreFavorite addFavorite(Long userId, Long offreId);
    void removeFavorite(Long userId, Long offreId);
}
