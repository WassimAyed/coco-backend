package tn.esprit.collocationservice.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.collocationservice.Dto.CollocOffreDTO;
import tn.esprit.collocationservice.Dto.CollocOffreRequestDTO;
import tn.esprit.collocationservice.Entity.UserActivityLog;
import tn.esprit.collocationservice.Entity.collocOffre;
import tn.esprit.collocationservice.Entity.collocOffreFilter;
import tn.esprit.collocationservice.Entity.collocOffreRequest;
import tn.esprit.collocationservice.Mapper.CollocOffreMapper;
import tn.esprit.collocationservice.Mapper.CollocOffreRequestMapper;
import tn.esprit.collocationservice.Repository.UserActivityLogRepository;
import tn.esprit.collocationservice.Service.collocOffreRequestService;
import tn.esprit.collocationservice.Service.collocOffreService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/collocation")
@RequiredArgsConstructor
public class CollocationController {

    private final collocOffreService collocationOffreService;
    private final collocOffreRequestService collocOffreRequestService;
    private final UserActivityLogRepository userActivityLogRepository;

    @PostMapping("/offresCollocCreate")
    public ResponseEntity<String> createOffer(
            @RequestPart("offre") CollocOffreDTO dto,
            @RequestPart(name = "imagesColloc", required = false) List<MultipartFile> files,
            @RequestParam("userId") Long idUser
    ) {
        collocationOffreService.create(CollocOffreMapper.toEntity(dto), files != null ? files : List.of(), idUser);
        return ResponseEntity.ok("Offer created successfully");
    }

    @GetMapping("/offresCollocGetAll")
    public ResponseEntity<List<CollocOffreDTO>> getAllOffreColloc() {
        return ResponseEntity.ok(collocationOffreService.getAll().stream()
                .map(CollocOffreMapper::toDTO)
                .toList());
    }

    @GetMapping("/offres/searchColloc")
    public Page<CollocOffreDTO> searchOffreColloc(
            collocOffreFilter filter,
            Pageable pageable) {
        return collocationOffreService.search(filter, pageable).map(CollocOffreMapper::toDTO);
    }

    @PostMapping("/requests")
    public ResponseEntity<String> requestCollocOffres(
            @RequestBody CollocOffreRequestDTO dto,
            @RequestHeader("X-USER-ID") Long user) {
        if (dto.getOfferId() == null) {
            throw new IllegalArgumentException("Offer ID is missing");
        }
        collocOffreRequest entity = CollocOffreRequestMapper.toEntity(dto);
        collocOffre offer = new collocOffre();
        offer.setId(dto.getOfferId());
        entity.setOffer(offer);

        collocOffreRequestService.create(entity, user);
        return ResponseEntity.ok("Request sent");
    }

    @GetMapping("/offresColloc/{id}")
    public ResponseEntity<CollocOffreDTO> getOfferById(@PathVariable Long id, @RequestHeader(value = "X-USER-ID", required = false) Long userId) {
        if (userId != null) {
            logActivity(userId, id);
        }
        return ResponseEntity.ok(CollocOffreMapper.toDTO(collocationOffreService.getById(id)));
    }

    private void logActivity(Long userId, Long offerId) {
        try {
            UserActivityLog logActivity = new UserActivityLog();
            logActivity.setUserId(userId);
            logActivity.setOfferId(offerId);
            logActivity.setActivityType(UserActivityLog.ActivityType.VIEW);
            logActivity.setTimestamp(LocalDateTime.now());
            userActivityLogRepository.save(logActivity);
        } catch (Exception e) {
            log.error("Failed to log activity for user {} and offer {}", userId, offerId, e);
        }
    }

    @GetMapping("/myOffresColloc/{ownerId}")
    public ResponseEntity<List<CollocOffreDTO>> getAllOffreCollocByOwnerId(@PathVariable Long ownerId) {
        return ResponseEntity.ok(collocationOffreService.findByOwnerId(ownerId).stream()
                .map(CollocOffreMapper::toDTO)
                .toList());
    }

    @PutMapping("/updateOffreColloc/{id}")
    public ResponseEntity<CollocOffreDTO> updateOffre(
            @PathVariable Long id,
            @RequestBody CollocOffreDTO dto) {
        collocOffre updated = collocationOffreService.updateOffre(id, CollocOffreMapper.toEntity(dto));
        return ResponseEntity.ok(CollocOffreMapper.toDTO(updated));
    }

    @DeleteMapping("/deleteOffreColloc/{id}")
    public ResponseEntity<String> deleteOffre(@PathVariable Long id) {
        collocationOffreService.deleteOffre(id);
        return ResponseEntity.ok("Offre deleted successfully");
    }

    @GetMapping("/nearby")
    public List<CollocOffreDTO> getNearbyOffers(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam double radius
    ) {
        return collocationOffreService.getNearbyOffers(lat, lng, radius).stream()
                .map(CollocOffreMapper::toDTO)
                .toList();
    }
}
