package tn.esprit.serviceetudiant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.serviceetudiant.dto.CoverUploadResponse;
import tn.esprit.serviceetudiant.dto.MlAnalysisRequest;
import tn.esprit.serviceetudiant.dto.StudentServiceResponse;
import tn.esprit.serviceetudiant.dto.StudentServiceUpsertRequest;
import tn.esprit.serviceetudiant.entity.ChatConversation;
import tn.esprit.serviceetudiant.entity.StudentService;
import tn.esprit.serviceetudiant.enums.DeliveryMode;
import tn.esprit.serviceetudiant.enums.ServiceCategory;
import tn.esprit.serviceetudiant.enums.ServiceModerationStatus;
import tn.esprit.serviceetudiant.exception.NotFoundException;
import tn.esprit.serviceetudiant.mapper.StudentServiceMapper;
import tn.esprit.serviceetudiant.repository.ChatConversationRepository;
import tn.esprit.serviceetudiant.repository.ChatMessageRepository;
import tn.esprit.serviceetudiant.repository.ServiceRequestRepository;
import tn.esprit.serviceetudiant.repository.StudentServiceRepository;
import tn.esprit.serviceetudiant.validation.Validators;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceService {

    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d+(?:[\\.,]\\d+)?)");

    private final StudentServiceRepository studentServiceRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ChatConversationRepository chatConversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final StorageGatewayService storageGatewayService;
    private final MlModerationClient mlModerationClient;
    private final StudentServiceMapper studentServiceMapper;

    @Transactional(readOnly = true)
    public List<StudentServiceResponse> getServices(String search, String categoryToken, String deliveryModeToken, Boolean featuredOnly) {
        return filterServices(search, categoryToken, deliveryModeToken, featuredOnly, ServiceModerationStatus.APPROVED);
    }

    @Transactional(readOnly = true)
    public List<StudentServiceResponse> getAdminServices(String search, String categoryToken, String deliveryModeToken, String moderationStatusToken, Boolean featuredOnly) {
        return filterServices(search, categoryToken, deliveryModeToken, featuredOnly, parseModerationStatus(moderationStatusToken));
    }

    @Transactional(readOnly = true)
    public StudentServiceResponse getServiceById(Long id) {
        Validators.requirePositive(id, "id");
        return studentServiceMapper.toResponse(findService(id));
    }

    @Transactional(readOnly = true)
    public StudentServiceResponse getServiceBySlug(String slug) {
        Validators.requireNonBlank(slug, "slug");
        return studentServiceRepository.findBySlug(slug)
                .map(studentServiceMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Student service not found."));
    }

    @Transactional(readOnly = true)
    public List<StudentServiceResponse> getServicesByProvider(Long providerId) {
        Validators.requirePositive(providerId, "providerId");
        return studentServiceRepository.findByProviderIdOrderByUpdatedAtDesc(providerId)
                .stream()
                .map(studentServiceMapper::toResponse)
                .toList();
    }

    @Transactional
    public StudentServiceResponse createService(StudentServiceUpsertRequest request) {
        Validators.requireNonNull(request, "request");
        Validators.requirePositiveOrNull(request.providerId(), "providerId");
        Validators.requireListSize(request.tags(), Validators.MAX_TAG_COUNT, "tags");
        if (request.tags() != null) {
            request.tags().forEach(tag -> Validators.requireMaxLength(tag, Validators.MAX_TAG_LENGTH, "tag"));
        }

        Instant now = Instant.now();
        StudentService service = StudentService.builder()
                .createdAt(now)
                .updatedAt(now)
                .requestCount(0)
                .moderationStatus(ServiceModerationStatus.PENDING)
                .moderatedAt(null)
                .build();

        applyUpsertRequest(service, request, null);
        StudentService saved = studentServiceRepository.save(service);
        try {
            mlModerationClient.analyzeAndModerate(saved.getId(), buildMlRequest(saved));
        } catch (Exception ex) {
            log.warn("[ML] Failed to schedule moderation for service id={}: {}", saved.getId(), ex.getMessage());
        }
        return studentServiceMapper.toResponse(saved);
    }

    @Transactional
    public StudentServiceResponse updateService(Long id, StudentServiceUpsertRequest request) {
        Validators.requirePositive(id, "id");
        Validators.requireNonNull(request, "request");
        Validators.requirePositiveOrNull(request.providerId(), "providerId");
        Validators.requireListSize(request.tags(), Validators.MAX_TAG_COUNT, "tags");
        if (request.tags() != null) {
            request.tags().forEach(tag -> Validators.requireMaxLength(tag, Validators.MAX_TAG_LENGTH, "tag"));
        }

        StudentService service = findService(id);
        applyUpsertRequest(service, request, id);
        service.setUpdatedAt(Instant.now());
        service.setModerationStatus(ServiceModerationStatus.PENDING);
        service.setModeratedAt(null);
        StudentService saved = studentServiceRepository.save(service);
        try {
            mlModerationClient.analyzeAndModerate(saved.getId(), buildMlRequest(saved));
        } catch (Exception ex) {
            log.warn("[ML] Failed to schedule moderation for service id={}: {}", saved.getId(), ex.getMessage());
        }
        return studentServiceMapper.toResponse(saved);
    }

    /** Builds the ML analysis request from the saved entity. */
    private static MlAnalysisRequest buildMlRequest(StudentService s) {
        return MlAnalysisRequest.builder()
                .title(s.getTitle())
                .shortDescription(s.getShortDescription())
                .price(s.getPriceValue() != null ? s.getPriceValue().doubleValue() : 0.0)
                .category(s.getCategory().name())
                .imageUrl(s.getCoverImageUrl() != null ? s.getCoverImageUrl() : "")
                .deliveryMode(s.getDeliveryMode() != null ? s.getDeliveryMode().name() : "")
                .tags(s.getTags())
                .build();
    }

    @Transactional
    public StudentServiceResponse moderateService(Long id, ServiceModerationStatus status) {
        Validators.requirePositive(id, "id");
        Validators.requireNonNull(status, "status");

        StudentService service = findService(id);
        service.setModerationStatus(status);
        service.setModeratedAt(Instant.now());
        service.setUpdatedAt(Instant.now());
        return studentServiceMapper.toResponse(studentServiceRepository.save(service));
    }

    @Transactional
    public StudentServiceResponse updateServiceTags(Long id, List<String> tags) {
        Validators.requirePositive(id, "id");
        Validators.requireListSize(tags, Validators.MAX_TAG_COUNT, "tags");
        if (tags != null) {
            tags.forEach(tag -> Validators.requireMaxLength(tag, Validators.MAX_TAG_LENGTH, "tag"));
        }

        StudentService service = findService(id);
        service.setTags(normalizeTags(tags));
        service.setUpdatedAt(Instant.now());
        return studentServiceMapper.toResponse(studentServiceRepository.save(service));
    }

    @Transactional
    public void deleteService(Long id) {
        Validators.requirePositive(id, "id");
        StudentService service = findService(id);
        List<Long> conversationIds = chatConversationRepository.findByServiceId(id)
                .stream()
                .map(ChatConversation::getId)
                .filter(Objects::nonNull)
                .toList();

        if (!conversationIds.isEmpty()) {
            chatMessageRepository.deleteByConversationIdIn(conversationIds);
        }

        chatConversationRepository.deleteByServiceId(id);
        serviceRequestRepository.deleteByServiceId(id);
        studentServiceRepository.delete(service);
    }

    @Transactional
    public CoverUploadResponse uploadCoverImage(MultipartFile file, Long ownerId) {
        Validators.requireImage(file, Validators.MAX_IMAGE_BYTES);
        Validators.requirePositive(ownerId, "ownerId");
        return storageGatewayService.uploadBanner(file, ownerId);
    }

    @Transactional
    public void refreshRequestMetrics(Long serviceId) {
        Validators.requirePositive(serviceId, "serviceId");
        StudentService service = findService(serviceId);
        long requestCount = serviceRequestRepository.countByServiceId(serviceId);
        service.setRequestCount((int) requestCount);
        service.setUpdatedAt(Instant.now());
        studentServiceRepository.save(service);
    }

    @Transactional(readOnly = true)
    public StudentService findService(Long id) {
        Validators.requirePositive(id, "id");
        return studentServiceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student service not found."));
    }

    public ServiceCategory parseCategory(String rawValue) {
        if (rawValue == null || rawValue.isBlank() || rawValue.equalsIgnoreCase("all")) {
            return null;
        }

        String normalized = normalizeEnumToken(rawValue);
        return switch (normalized) {
            case "ACADEMIC" -> ServiceCategory.ACADEMIC;
            case "MOBILITY" -> ServiceCategory.MOBILITY;
            case "ERRANDS" -> ServiceCategory.ERRANDS;
            case "CREATIVE" -> ServiceCategory.CREATIVE;
            case "TECH" -> ServiceCategory.TECH;
            case "WELLBEING" -> ServiceCategory.WELLBEING;
            default -> throw new IllegalArgumentException("Unsupported service category: " + rawValue);
        };
    }

    public DeliveryMode parseDeliveryMode(String rawValue) {
        if (rawValue == null || rawValue.isBlank() || rawValue.equalsIgnoreCase("all")) {
            return null;
        }

        String normalized = normalizeEnumToken(rawValue);
        return switch (normalized) {
            case "ONLINE" -> DeliveryMode.ONLINE;
            case "ON_SITE", "ONSITE" -> DeliveryMode.ON_SITE;
            case "HYBRID" -> DeliveryMode.HYBRID;
            default -> throw new IllegalArgumentException("Unsupported delivery mode: " + rawValue);
        };
    }

    public ServiceModerationStatus parseModerationStatus(String rawValue) {
        if (rawValue == null || rawValue.isBlank() || rawValue.equalsIgnoreCase("all")) {
            return null;
        }

        String normalized = normalizeEnumToken(rawValue);
        return switch (normalized) {
            case "PENDING" -> ServiceModerationStatus.PENDING;
            case "APPROVED" -> ServiceModerationStatus.APPROVED;
            case "REJECTED" -> ServiceModerationStatus.REJECTED;
            default -> throw new IllegalArgumentException("Unsupported moderation status: " + rawValue);
        };
    }

    private List<StudentServiceResponse> filterServices(String search, String categoryToken, String deliveryModeToken, Boolean featuredOnly, ServiceModerationStatus moderationStatus) {
        ServiceCategory category = parseCategory(categoryToken);
        DeliveryMode deliveryMode = parseDeliveryMode(deliveryModeToken);
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        boolean featured = Boolean.TRUE.equals(featuredOnly);

        return studentServiceRepository.findAll(Sort.by(Sort.Order.asc("moderationStatus"), Sort.Order.desc("featured"), Sort.Order.desc("updatedAt"))).stream()
                .filter(service -> moderationStatus == null || service.getModerationStatus() == moderationStatus)
                .filter(service -> normalizedSearch.isBlank() || matchesSearch(service, normalizedSearch))
                .filter(service -> category == null || service.getCategory() == category)
                .filter(service -> deliveryMode == null || service.getDeliveryMode() == deliveryMode)
                .filter(service -> !featured || service.isFeatured())
                .map(studentServiceMapper::toResponse)
                .toList();
    }

    private void applyUpsertRequest(StudentService service, StudentServiceUpsertRequest request, Long existingId) {
        String slug = buildUniqueSlug(request.title(), existingId);

        service.setTitle(request.title().trim());
        service.setSlug(slug);
        service.setShortDescription(request.shortDescription().trim());
        service.setCategory(request.category());
        service.setPriceLabel(request.priceLabel().trim());
        service.setPriceValue(parsePriceValue(request.priceLabel()));
        service.setDeliveryMode(request.deliveryMode());
        service.setTags(normalizeTags(request.tags()));
        service.setLocation(request.location().trim());
        service.setProviderId(request.providerId() == null ? 0L : request.providerId());
        service.setProviderName(defaultIfBlank(request.providerName(), "Student Provider"));
        service.setProviderHeadline(defaultIfBlank(request.providerHeadline(), "Helpful student service provider."));
        service.setProviderAvatar(defaultIfBlank(request.providerAvatar(), "https://ui-avatars.com/api/?name=Student+Provider&background=7f1d1d&color=fff"));
        service.setProviderDepartment(defaultIfBlank(request.providerDepartment(), "ESPRIT"));
        service.setCoverImageUrl(defaultIfBlank(request.coverImageUrl(), "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=1200&q=80"));
        service.setFeatured(Boolean.TRUE.equals(request.featured()));
        if (service.getModerationStatus() == null) {
            service.setModerationStatus(ServiceModerationStatus.PENDING);
        }
    }

    private String buildUniqueSlug(String title, Long existingId) {
        String baseSlug = title.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        String candidate = baseSlug.isBlank() ? "student-service" : baseSlug;
        int suffix = 2;

        while (existingId == null ? studentServiceRepository.existsBySlug(candidate) : studentServiceRepository.existsBySlugAndIdNot(candidate, existingId)) {
            candidate = baseSlug + "-" + suffix++;
        }

        return candidate;
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }

        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean matchesSearch(StudentService service, String normalizedSearch) {
        return service.getTitle().toLowerCase(Locale.ROOT).contains(normalizedSearch)
                || service.getShortDescription().toLowerCase(Locale.ROOT).contains(normalizedSearch)
                || service.getProviderName().toLowerCase(Locale.ROOT).contains(normalizedSearch)
                || service.getTags().stream().anyMatch(tag -> tag.toLowerCase(Locale.ROOT).contains(normalizedSearch));
    }

    private String normalizeEnumToken(String rawValue) {
        return rawValue.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private BigDecimal parsePriceValue(String priceLabel) {
        if (priceLabel == null || priceLabel.isBlank()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        Matcher matcher = PRICE_PATTERN.matcher(priceLabel.replace(',', '.'));
        if (!matcher.find()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        try {
            return new BigDecimal(matcher.group(1)).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }

}
