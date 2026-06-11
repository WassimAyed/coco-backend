package tn.esprit.serviceetudiant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.serviceetudiant.dto.ServiceRequestCreateRequest;
import tn.esprit.serviceetudiant.dto.ServiceRequestResponse;
import tn.esprit.serviceetudiant.dto.ServiceRequestStatusUpdateRequest;
import tn.esprit.serviceetudiant.service.ServiceRequestService;

import java.util.List;

@RestController
@RequestMapping("/student-services")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    @PostMapping("/{serviceId}/requests")
    public ResponseEntity<ServiceRequestResponse> createRequest(
            @PathVariable Long serviceId,
            @Valid @RequestBody ServiceRequestCreateRequest request
    ) {
        return ResponseEntity.ok(serviceRequestService.createRequest(serviceId, request));
    }

    @GetMapping("/requests/requester/{requesterId}")
    public ResponseEntity<List<ServiceRequestResponse>> getRequesterRequests(@PathVariable Long requesterId) {
        return ResponseEntity.ok(serviceRequestService.getRequestsByRequester(requesterId));
    }

    @GetMapping("/requests/provider/{providerId}")
    public ResponseEntity<List<ServiceRequestResponse>> getProviderRequests(@PathVariable Long providerId) {
        return ResponseEntity.ok(serviceRequestService.getRequestsByProvider(providerId));
    }

    @GetMapping("/{serviceId}/requests/requester/{requesterId}")
    public ResponseEntity<ServiceRequestResponse> getMyRequestForService(
            @PathVariable Long serviceId,
            @PathVariable Long requesterId
    ) {
        return ResponseEntity.ok(serviceRequestService.getRequesterRequestForService(serviceId, requesterId));
    }

    @PutMapping("/requests/{requestId}/status")
    public ResponseEntity<ServiceRequestResponse> updateRequestStatus(
            @PathVariable Long requestId,
            @Valid @RequestBody ServiceRequestStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(serviceRequestService.updateRequestStatus(requestId, request.status()));
    }
}