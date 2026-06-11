package tn.esprit.collocationservice.Service;

import tn.esprit.collocationservice.Entity.collocOffreRequest;

import java.util.List;

public interface collocOffreRequestService {

    collocOffreRequest create(collocOffreRequest cor, Long userId);

    List<collocOffreRequest> getRequestsByStudent(Long studentId);

    collocOffreRequest updateStatus(Long id, collocOffreRequest.Status status);

    List<collocOffreRequest> getRequestsByOfferOwner(Long ownerId);

    void deleteRequest(Long id);

    public List<collocOffreRequest> getRequestsByOfferIds(List<Long> offerIds, Long ownerId) ;
    }