package tn.esprit.lostfoundservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;


@Entity
@Table(name = "lost_item", indexes = {
    @Index(name = "idx_lost_item_user_id", columnList = "userId"),
    @Index(name = "idx_lost_item_status", columnList = "status"),
    @Index(name = "idx_lost_item_type", columnList = "type"),
    @Index(name = "idx_lost_item_category", columnList = "category"),
    @Index(name = "idx_lost_item_location", columnList = "location"),
    @Index(name = "idx_lost_item_date_time", columnList = "dateTime")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LostItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LostItemType type;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private String contactInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LostItemStatus status;

    @Column(nullable = false)
    private Long userId;

    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;
}
