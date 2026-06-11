package tn.esprit.collocationservice.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.collocationservice.Entity.collocOffre;
import tn.esprit.collocationservice.Entity.collocOffreRequest;
import tn.esprit.collocationservice.Exception.OfferNotFoundException;
import tn.esprit.collocationservice.Exception.RequestNotFoundException;
import tn.esprit.collocationservice.Repository.collocOffreRepo;
import tn.esprit.collocationservice.Repository.collocOffreRequestRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class collocOffreRequestServiceImpl implements collocOffreRequestService {

    private final collocOffreRequestRepo repo;
    private final collocOffreRepo offreRepo;

    @Override
    public collocOffreRequest create(collocOffreRequest cor, Long userId) {
        if (cor.getOffer() == null || cor.getOffer().getId() == null) {
            throw new IllegalArgumentException("Offer information is missing");
        }

        collocOffre offer = offreRepo.findById(cor.getOffer().getId())
                .orElseThrow(() -> new OfferNotFoundException("Offer not found with id " + cor.getOffer().getId()));
        
        cor.setOffer(offer);
        cor.setStudentId(userId);
        cor.setStatus(collocOffreRequest.Status.ENCOURS);
        cor.setCreatedAt(LocalDateTime.now());

        return repo.save(cor);
    }

    @Override
    public List<collocOffreRequest> getRequestsByStudent(Long studentId) {
        return repo.findByStudentId(studentId);
    }

    @Override
    public collocOffreRequest updateStatus(Long id, collocOffreRequest.Status status) {
        collocOffreRequest request = repo.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Request not found with id " + id));
        request.setStatus(status);
        return repo.save(request);
    }

    @Override
    public List<collocOffreRequest> getRequestsByOfferOwner(Long ownerId) {
        return repo.findByOfferOwnerId(ownerId);
    }

    @Override
    public void deleteRequest(Long id) {
        if (!repo.existsById(id)) {
            throw new RequestNotFoundException("Request not found with id " + id);
        }
        repo.deleteById(id);
    }

    @Override
    public List<collocOffreRequest> getRequestsByOfferIds(List<Long> offerIds, Long ownerId) {
        return repo.findAll().stream()
                .filter(r -> offerIds.contains(r.getOffer().getId()))
                .filter(r -> r.getOffer().getOwnerId().equals(ownerId))
                .toList();
    }
}