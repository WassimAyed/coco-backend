package tn.esprit.collocationservice.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.collocationservice.Entity.collocOffre;
import tn.esprit.collocationservice.Entity.collocOffreFilter;
import tn.esprit.collocationservice.Entity.collocOffreImage;
import tn.esprit.collocationservice.Entity.collocOffreSpecifications;
import tn.esprit.collocationservice.Exception.ImageStorageException;
import tn.esprit.collocationservice.Exception.OfferNotFoundException;
import tn.esprit.collocationservice.Repository.collocOffreImageRepo;
import tn.esprit.collocationservice.Repository.collocOffreRepo;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class collocOffreServiceImpl implements collocOffreService {

    private final collocOffreRepo repo;
    private final collocOffreImageRepo imageRepo;
    private final FileStorageService storageService;

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/png", "image/jpg", "image/jpeg", "image/gif"
    );

    @Override
    public collocOffre create(collocOffre co, List<MultipartFile> images, Long userId) {
        collocOffre offer = repo.save(initializeOffer(co, userId));

        if (images != null && !images.isEmpty()) {
            processImages(offer, images);
        }

        return offer;
    }

    private collocOffre initializeOffer(collocOffre co, Long userId) {
        collocOffre offer = new collocOffre();
        offer.setTitre(co.getTitre());
        offer.setDescription(co.getDescription());
        offer.setPrixLoc(co.getPrixLoc());
        offer.setVille(co.getVille());
        offer.setChambres(co.getChambres());
        offer.setMeublee(co.getMeublee());
        offer.setLatitude(co.getLatitude());
        offer.setLongitude(co.getLongitude());
        offer.setOwnerId(userId);
        offer.setCreatedAt(LocalDate.now());
        offer.setExpiryDate(co.getExpiryDate());
        return offer;
    }

    private void processImages(collocOffre offer, List<MultipartFile> images) {
        for (MultipartFile file : images) {
            if (!file.isEmpty()) {
                handleImageUpload(offer, file);
            }
        }
    }

    private void handleImageUpload(collocOffre offer, MultipartFile file) {
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Unsupported file type: " + file.getContentType());
        }

        try {
            String filename = storageService.store(file);
            saveImageMetadata(offer, filename);
        } catch (IOException e) {
            log.error("Failed to store image {}", file.getOriginalFilename(), e);
            throw new ImageStorageException("Failed to store image", e);
        }
    }

    private void saveImageMetadata(collocOffre offer, String uploadedFilename) {
        collocOffreImage img = new collocOffreImage();
        img.setFilename(uploadedFilename);
        img.setUrl("http://localhost:9092/api/collocation/imagesColloc/" + uploadedFilename);
        img.setOffre(offer);
        imageRepo.save(img);
    }

    @Override
    public List<collocOffre> getAll() {
        return repo.findAll();
    }

    @Override
    public Page<collocOffre> search(collocOffreFilter filter, Pageable pageable) {
        return repo.findAll(collocOffreSpecifications.filter(filter), pageable);
    }

    @Override
    public collocOffre getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Offer not found with id " + id));
    }

    @Override
    public List<collocOffre> findByOwnerId(Long id) {
        return repo.findByOwnerId(id);
    }

    @Override
    public collocOffre updateOffre(Long id, collocOffre updatedOffre) {
        collocOffre existing = repo.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Offre not found with id : " + id));

        existing.setTitre(updatedOffre.getTitre());
        existing.setDescription(updatedOffre.getDescription());
        existing.setPrixLoc(updatedOffre.getPrixLoc());
        existing.setVille(updatedOffre.getVille());
        existing.setChambres(updatedOffre.getChambres());
        existing.setMeublee(updatedOffre.getMeublee());
        existing.setLatitude(updatedOffre.getLatitude());
        existing.setLongitude(updatedOffre.getLongitude());
        existing.setExpiryDate(updatedOffre.getExpiryDate());

        return repo.save(existing);
    }

    @Override
    public void deleteOffre(Long id) {
        collocOffre offre = repo.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Offre not found with id : " + id));
        repo.delete(offre);
    }

    @Override
    public List<collocOffre> getNearbyOffers(double lat, double lng, double radius) {
        return repo.findNearbyOffers(lat, lng, radius);
    }
}
