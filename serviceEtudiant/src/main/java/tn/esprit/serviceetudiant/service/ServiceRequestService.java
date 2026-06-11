package tn.esprit.serviceetudiant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.serviceetudiant.dto.ServiceRequestCreateRequest;
import tn.esprit.serviceetudiant.dto.ServiceRequestResponse;
import tn.esprit.serviceetudiant.entity.ServiceRequest;
import tn.esprit.serviceetudiant.entity.StudentService;
import tn.esprit.serviceetudiant.enums.ServiceModerationStatus;
import tn.esprit.serviceetudiant.enums.ServiceRequestStatus;
import tn.esprit.serviceetudiant.exception.ConflictException;
import tn.esprit.serviceetudiant.exception.NotFoundException;
import tn.esprit.serviceetudiant.mapper.ServiceRequestMapper;
import tn.esprit.serviceetudiant.repository.ServiceRequestRepository;
import tn.esprit.serviceetudiant.validation.Validators;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final StudentServiceService studentServiceService;
    private final ChatService chatService;
    private final ServiceRequestMapper serviceRequestMapper;

    @Transactional
    public ServiceRequestResponse createRequest(Long serviceId, ServiceRequestCreateRequest request) {
        Validators.requirePositive(serviceId, "serviceId");
        Validators.requireNonNull(request, "request");
        Validators.requirePositive(request.requesterId(), "requesterId");
        Validators.requireMaxLength(request.preferredDate(), Validators.MAX_PREFERRED_DATE_LENGTH, "preferredDate");

        StudentService service = studentServiceService.findService(serviceId);
        if (service.getModerationStatus() != ServiceModerationStatus.APPROVED) {
            throw new ConflictException("This student service is not available for requests yet.");
        }

        if (service.getProviderId().equals(request.requesterId())) {
            throw new ConflictException("You cannot request your own student service.");
        }

        serviceRequestRepository.findByServiceIdAndRequesterId(serviceId, request.requesterId())
                .ifPresent(existing -> {
                    throw new ConflictException("You already sent a request for this student service.");
                });

        ServiceRequest serviceRequest = ServiceRequest.builder()
                .serviceId(service.getId())
                .serviceTitle(service.getTitle())
                .serviceCategory(service.getCategory())
                .requesterId(request.requesterId())
                .requesterName(request.requesterName().trim())
                .requesterDepartment(defaultIfBlank(request.requesterDepartment(), "ESPRIT Student"))
                .requesterAvatar(defaultIfBlank(request.requesterAvatar(), "https://ui-avatars.com/api/?name=ESPRIT+Student&background=7f1d1d&color=fff"))
                .providerId(service.getProviderId())
                .providerName(service.getProviderName())
                .message(request.message().trim())
                .preferredDate(request.preferredDate().trim())
                .status(ServiceRequestStatus.PENDING)
                .budgetLabel(request.budgetLabel().trim())
                .createdAt(Instant.now())
                .build();

        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);
        studentServiceService.refreshRequestMetrics(serviceId);
        return serviceRequestMapper.toResponse(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getRequestsByRequester(Long requesterId) {
        Validators.requirePositive(requesterId, "requesterId");
        return serviceRequestRepository.findByRequesterIdOrderByCreatedAtDesc(requesterId)
                .stream()
                .map(serviceRequestMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getRequestsByProvider(Long providerId) {
        Validators.requirePositive(providerId, "providerId");
        return serviceRequestRepository.findByProviderIdOrderByCreatedAtDesc(providerId)
                .stream()
                .map(serviceRequestMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceRequestResponse getRequesterRequestForService(Long serviceId, Long requesterId) {
        Validators.requirePositive(serviceId, "serviceId");
        Validators.requirePositive(requesterId, "requesterId");
        return serviceRequestRepository.findByServiceIdAndRequesterId(serviceId, requesterId)
                .map(serviceRequestMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Service request not found."));
    }

    @Transactional
    public ServiceRequestResponse updateRequestStatus(Long requestId, ServiceRequestStatus status) {
        Validators.requirePositive(requestId, "requestId");
        Validators.requireNonNull(status, "status");

        ServiceRequest request = findRequest(requestId);
        request.setStatus(status);
        ServiceRequest savedRequest = serviceRequestRepository.save(request);

        if (status == ServiceRequestStatus.ACCEPTED || status == ServiceRequestStatus.COMPLETED) {
            chatService.ensureConversationForAcceptedRequest(savedRequest);
        }

        return serviceRequestMapper.toResponse(savedRequest);
    }

    @Transactional(readOnly = true)
    public ServiceRequest findRequest(Long requestId) {
        Validators.requirePositive(requestId, "requestId");
        return serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Service request not found."));
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

}
