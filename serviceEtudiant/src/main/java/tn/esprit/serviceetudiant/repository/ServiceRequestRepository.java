package tn.esprit.serviceetudiant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.serviceetudiant.entity.ServiceRequest;

import java.util.List;
import java.util.Optional;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
    List<ServiceRequest> findByProviderIdOrderByCreatedAtDesc(Long providerId);
    Optional<ServiceRequest> findByServiceIdAndRequesterId(Long serviceId, Long requesterId);
    List<ServiceRequest> findByServiceIdOrderByCreatedAtDesc(Long serviceId);
    long countByServiceId(Long serviceId);
    void deleteByServiceId(Long serviceId);
}