package tn.esprit.serviceetudiant.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import tn.esprit.serviceetudiant.dto.MlAnalysisRequest;
import tn.esprit.serviceetudiant.dto.MlAnalysisResponse;
import tn.esprit.serviceetudiant.entity.StudentService;
import tn.esprit.serviceetudiant.enums.ServiceModerationStatus;
import tn.esprit.serviceetudiant.repository.StudentServiceRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Calls the ML moderation service asynchronously after a service is created or updated.
 *
 * Decision mapping:
 *   APPROVED     → ServiceModerationStatus.APPROVED  (auto-approved, visible in marketplace)
 *   NEEDS_REVIEW → ServiceModerationStatus.PENDING   (stays in admin queue for manual review)
 *   REJECTED     → ServiceModerationStatus.REJECTED  (hidden from marketplace)
 *
 * If the ML service is unreachable the service stays PENDING so admins can review it manually.
 * This class injects StudentServiceRepository directly (not StudentServiceService) to avoid
 * a circular Spring dependency.
 */
@Service
@Slf4j
public class MlModerationClient {

    private final RestTemplate restTemplate;
    private final StudentServiceRepository studentServiceRepository;

    @Value("${ml.service.url:http://localhost:8000}")
    private String mlServiceUrl;

    public MlModerationClient(
            RestTemplateBuilder builder,
            StudentServiceRepository studentServiceRepository,
            @Value("${ml.service.timeout-seconds:150}") int timeoutSeconds
    ) {
        // Dedicated RestTemplate with a generous timeout for Ollama inference.
        // Ollama can take 30-120 s on CPU; we give it 150 s before giving up.
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
        this.studentServiceRepository = studentServiceRepository;
    }

    /**
     * Fire-and-forget: analyse the saved service and update its moderation status.
     * Runs in the "moderationExecutor" thread pool so the HTTP response is never
     * visible to the student's original request.
     */
    @Async("moderationExecutor")
    public void analyzeAndModerate(Long serviceId, MlAnalysisRequest request) {
        log.info("[ML] Starting moderation for service id={} title={}", serviceId, request.title());

        MlAnalysisResponse response;
        try {
            response = restTemplate.postForObject(
                    mlServiceUrl + "/analyze",
                    request,
                    MlAnalysisResponse.class
            );
        } catch (HttpClientErrorException ex) {
            log.warn("[ML] ML service rejected request for service {} (HTTP {}): {}",
                    serviceId, ex.getStatusCode(), ex.getResponseBodyAsString());
            return;
        } catch (RestClientException ex) {
            log.warn("[ML] Could not reach ML service at {} — service {} stays PENDING. Reason: {}",
                    mlServiceUrl, serviceId, ex.getMessage());
            return;
        } catch (Exception ex) {
            log.error("[ML] Unexpected error during ML moderation for service {}: {}", serviceId, ex.getMessage(), ex);
            return;
        }

        if (response == null) {
            log.warn("[ML] ML service returned null response for service {} — stays PENDING.", serviceId);
            return;
        }

        ServiceModerationStatus newStatus = mapDecision(response.decision());

        Optional<StudentService> opt = studentServiceRepository.findById(serviceId);
        if (opt.isEmpty()) {
            log.warn("[ML] Service {} no longer exists, skipping moderation update.", serviceId);
            return;
        }

        StudentService service = opt.get();
        service.setModerationStatus(newStatus);
        service.setModeratedAt(Instant.now());
        studentServiceRepository.save(service);

        log.info("[ML] Service id={} → {} (confidence={}, flags={})",
                serviceId, newStatus, response.confidence(), response.flags());
    }

    /**
     * Maps the ML service decision string to the entity enum.
     * NEEDS_REVIEW stays as PENDING so it appears in the admin moderation queue.
     */
    private static ServiceModerationStatus mapDecision(String decision) {
        return switch (decision == null ? "" : decision.toUpperCase()) {
            case "APPROVED"     -> ServiceModerationStatus.APPROVED;
            case "REJECTED"     -> ServiceModerationStatus.REJECTED;
            default             -> ServiceModerationStatus.PENDING;  // NEEDS_REVIEW or unknown
        };
    }
}
