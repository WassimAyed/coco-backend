package tn.esprit.collocationservice.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.collocationservice.Entity.collocOffreFavorite;
import tn.esprit.collocationservice.Entity.collocOffre;
import tn.esprit.collocationservice.Exception.OfferNotFoundException;
import tn.esprit.collocationservice.Repository.collocOffreFavoriteRepo;
import tn.esprit.collocationservice.Repository.collocOffreRepo;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class collocOffreFavoriteServiceImpl implements collocOffreFavoriteService {

    private final collocOffreFavoriteRepo favoriteRepo;
    private final collocOffreRepo offreRepo;

    public List<collocOffreFavorite> getFavoritesByUser(Long userId) {
        return favoriteRepo.findByUserId(userId);
    }

    public collocOffreFavorite addFavorite(Long userId, Long offreId) {
        collocOffre offre = offreRepo.findById(offreId)
                .orElseThrow(() -> new OfferNotFoundException("Offer not found with id " + offreId));

        // Check if already favorited
        if (favoriteRepo.findByUserIdAndOffreId(userId, offreId).isPresent()) {
            throw new IllegalArgumentException("Already favorited");
        }

        collocOffreFavorite fav = new collocOffreFavorite();
        fav.setUserId(userId);
        fav.setOffre(offre);

        return favoriteRepo.save(fav);
    }

    @Transactional
    public void removeFavorite(Long userId, Long offreId) {
        favoriteRepo.deleteByUserIdAndOffreId(userId, offreId);
        log.info("Deleted favorite for user: {} and offer: {}", userId, offreId);
    }



}
