package tn.esprit.serviceetudiant.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.serviceetudiant.enums.DeliveryMode;
import tn.esprit.serviceetudiant.enums.ServiceCategory;
import tn.esprit.serviceetudiant.enums.ServiceModerationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student_services")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @Column(nullable = false, length = 280)
    private String shortDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ServiceCategory category;

    @Column(nullable = false, length = 80)
    private String priceLabel;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DeliveryMode deliveryMode;

    @ElementCollection
    @CollectionTable(name = "student_service_tags", joinColumns = @JoinColumn(name = "service_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(nullable = false, length = 160)
    private String location;

    @Column(nullable = false)
    private Long providerId;

    @Column(nullable = false, length = 120)
    private String providerName;

    @Column(nullable = false, length = 220)
    private String providerHeadline;

    @Column(nullable = false, length = 2048)
    private String providerAvatar;

    @Column(nullable = false, length = 120)
    private String providerDepartment;

    @Column(nullable = false, length = 2048)
    private String coverImageUrl;

    @Column(nullable = false)
    private boolean featured;

    @Column(nullable = false)
    private int requestCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private ServiceModerationStatus moderationStatus = ServiceModerationStatus.PENDING;

    private Instant moderatedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
