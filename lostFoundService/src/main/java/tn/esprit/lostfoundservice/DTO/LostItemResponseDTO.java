package tn.esprit.lostfoundservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.lostfoundservice.entity.LostItemStatus;
import tn.esprit.lostfoundservice.entity.LostItemType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LostItemResponseDTO {

    private Long id;
    private String title;
    private String description;
    private LostItemType type;
    private String category;
    private String location;
    private LocalDateTime dateTime;
    private String contactInfo;
    private LostItemStatus status;
    private Long userId;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
    private boolean isOwner; // Indique si l'utilisateur actuel est propriétaire
    
    private java.util.List<java.util.Map<String, Object>> aiProposals;
}
