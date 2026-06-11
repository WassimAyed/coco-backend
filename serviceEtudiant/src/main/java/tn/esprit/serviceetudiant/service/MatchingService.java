package tn.esprit.serviceetudiant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.serviceetudiant.dto.ServiceRecommendationResponse;
import tn.esprit.serviceetudiant.entity.StudentService;
import tn.esprit.serviceetudiant.repository.StudentServiceRepository;
import tn.esprit.serviceetudiant.validation.Validators;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final StudentServiceRepository studentServiceRepository;

    @Transactional(readOnly = true)
    public List<ServiceRecommendationResponse> getRecommendations(Long userId) {
        Validators.requirePositiveOrNull(userId, "userId");
        Stream<StudentService> services = studentServiceRepository.findAll(
                        Sort.by(
                                Sort.Order.desc("featured"),
                                Sort.Order.desc("requestCount"),
                                Sort.Order.desc("updatedAt")
                        )
                ).stream();

        if (userId != null) {
            services = services.filter(service -> !service.getProviderId().equals(userId));
        }

        return services
                .limit(6)
                .map(service -> new ServiceRecommendationResponse(
                        service.getId(),
                        service.getTitle(),
                        buildReason(service),
                        buildScore(service),
                        service.getTags().stream().limit(3).toList()
                ))
                .sorted(Comparator.comparingInt(ServiceRecommendationResponse::score).reversed())
                .toList();
    }

    private String buildReason(StudentService service) {
        if (service.isFeatured()) {
            return "Featured student service with strong engagement and consistent reviews.";
        }
        if (!service.getTags().isEmpty()) {
            return "Recommended because it matches trending student needs around " + String.join(", ", service.getTags().stream().limit(3).toList()) + ".";
        }
        return "Recommended because it is active, relevant, and recently updated by a trusted student provider.";
    }

    private int buildScore(StudentService service) {
        int featuredBoost = service.isFeatured() ? 15 : 0;
        int requestBoost = Math.min(service.getRequestCount(), 20);
        return Math.min(99, 45 + featuredBoost + requestBoost);
    }
}