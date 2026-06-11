package tn.esprit.serviceetudiant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.serviceetudiant.enums.ServiceCategory;
import tn.esprit.serviceetudiant.enums.ServiceRequestStatus;

import java.time.Instant;

@Entity
@Table(name = "service_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long serviceId;

    @Column(nullable = false, length = 160)
    private String serviceTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ServiceCategory serviceCategory;

    @Column(nullable = false)
    private Long requesterId;

    @Column(nullable = false, length = 120)
    private String requesterName;

    @Column(nullable = false, length = 120)
    private String requesterDepartment;

    @Column(nullable = false, length = 2048)
    private String requesterAvatar;

    @Column(nullable = false)
    private Long providerId;

    @Column(nullable = false, length = 120)
    private String providerName;

    @Column(nullable = false, length = 2500)
    private String message;

    @Column(nullable = false, length = 180)
    private String preferredDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ServiceRequestStatus status;

    @Column(nullable = false, length = 80)
    private String budgetLabel;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}