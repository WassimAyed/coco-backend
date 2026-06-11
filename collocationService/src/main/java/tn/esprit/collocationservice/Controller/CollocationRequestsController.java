package tn.esprit.collocationservice.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.collocationservice.Dto.CollocOffreRequestDTO;
import tn.esprit.collocationservice.Entity.collocOffre;
import tn.esprit.collocationservice.Entity.collocOffreRequest;
import tn.esprit.collocationservice.Mapper.CollocOffreRequestMapper;
import tn.esprit.collocationservice.Service.collocOffreRequestService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/collocation/requests")
@RequiredArgsConstructor
public class CollocationRequestsController {

    private final collocOffreRequestService service;

    @PostMapping("/create")
    public ResponseEntity<String> createRequest(
            @RequestBody CollocOffreRequestDTO dto,
            @RequestHeader("X-USER-ID") Long studentId
    ) {
        if (dto.getOfferId() == null) {
            throw new IllegalArgumentException("Offre manquante !");
        }

        collocOffreRequest entity = CollocOffreRequestMapper.toEntity(dto);
        collocOffre offer = new collocOffre();
        offer.setId(dto.getOfferId());
        entity.setOffer(offer);

        service.create(entity, studentId);
        return ResponseEntity.ok("Request sent");
    }

    @GetMapping("/my")
    public List<CollocOffreRequestDTO> getMyRequests(
            @RequestHeader("userId") Long userId
    ) {
        return service.getRequestsByStudent(userId).stream()
                .map(CollocOffreRequestMapper::toDTO)
                .toList();
    }

    @GetMapping("/forOwner")
    public List<CollocOffreRequestDTO> getRequestsForMyOffers(
            @RequestHeader("userId") Long ownerId
    ) {
        return service.getRequestsByOfferOwner(ownerId).stream()
                .map(CollocOffreRequestMapper::toDTO)
                .toList();
    }

    @PutMapping("/{id}/status")
    public CollocOffreRequestDTO updateRequestStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String statusStr = body.get("status");
        if (statusStr == null) {
            throw new IllegalArgumentException("Status is missing");
        }
        collocOffreRequest.Status status =
                collocOffreRequest.Status.valueOf(statusStr.toUpperCase());

        return CollocOffreRequestMapper.toDTO(service.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRequest(@PathVariable Long id) {
        service.deleteRequest(id);
        return ResponseEntity.ok("Request deleted");
    }

    @PostMapping("/byOfferIds")
    public List<CollocOffreRequestDTO> getRequestsByOfferIds(
            @RequestBody List<Long> offerIds,
            @RequestHeader("userId") Long ownerId
    ) {
        return service.getRequestsByOfferIds(offerIds, ownerId).stream()
                .map(CollocOffreRequestMapper::toDTO)
                .toList();
    }
}
