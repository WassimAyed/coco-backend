package tn.esprit.lostfoundservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.lostfoundservice.DTO.LostItemRequestDTO;
import tn.esprit.lostfoundservice.DTO.LostItemResponseDTO;
import tn.esprit.lostfoundservice.entity.LostItem;
import tn.esprit.lostfoundservice.entity.LostItemStatus;
import tn.esprit.lostfoundservice.entity.LostItemType;
import tn.esprit.lostfoundservice.exception.ItemNotFoundException;
import tn.esprit.lostfoundservice.exception.UnauthorizedAccessException;
import tn.esprit.lostfoundservice.repository.LostItemRepository;
import tn.esprit.lostfoundservice.specification.LostItemSpecifications;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LostItemService {

    private final LostItemRepository repository;
    private final AiSimilarityClient aiSimilarityClient;

    @Value("${lostfound.image.storage-path:./uploads/lostfound}")
    private String imageStoragePath;

    private String toLocalImagePath(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        String[] parts = imageUrl.trim().split("/");
        String fileName = parts[parts.length - 1];
        Path uploadDir = Paths.get(imageStoragePath).toAbsolutePath().normalize();
        return uploadDir.resolve(fileName).toString();
    }

    /**
     * Create a new lost/found item
     */
    @Transactional
    public LostItemResponseDTO createItem(LostItemRequestDTO requestDTO, Long userId) {
        log.info("Creating new item for user {}", userId);

        LostItem item = LostItem.builder()
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .type(requestDTO.getType())
                .category(requestDTO.getCategory())
                .location(requestDTO.getLocation())
                .contactInfo(requestDTO.getContactInfo())
                .imageUrl(requestDTO.getImageUrl() != null ? requestDTO.getImageUrl().trim() : null)
                .userId(userId)
                .status(LostItemStatus.ACTIVE)
                .dateTime(LocalDateTime.now())
                .build();

        LostItem savedItem = repository.save(item);
        log.info("Item created with id: {}", savedItem.getId());
        
        LostItemResponseDTO responseDTO = mapToResponseDTO(savedItem, userId, true);
        
        if (savedItem.getImageUrl() != null && !savedItem.getImageUrl().isEmpty()) {
            String localPath = toLocalImagePath(savedItem.getImageUrl());
            
            // 1. Indexer la nouvelle image dans FAISS
            aiSimilarityClient.indexItem(
                    savedItem.getId(),
                    savedItem.getType().name(),
                    savedItem.getTitle(),
                    savedItem.getDescription(),
                    savedItem.getCategory(),
                    localPath
            );
            
            // 2. Chercher dans la base opposée (proposer des résultats)
            List<java.util.Map<String, Object>> proposals = aiSimilarityClient.proposeSimilarItems(
                    savedItem.getId(),
                    savedItem.getType().name(),
                    savedItem.getCategory(),
                    savedItem.getTitle(),
                    savedItem.getDescription(),
                    localPath,
                    5 // top 5
            );
            responseDTO.setAiProposals(proposals);
        }

        return responseDTO;
    }

    /**
     * Get item by ID
     */
    public LostItemResponseDTO getItemById(Long id, Long userId) {
        log.info("Fetching item with id: {}", id);
        LostItem item = repository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + id));
        boolean isOwner = userId != null && item.getUserId().equals(userId);

        if (item.getStatus() == LostItemStatus.BLOCKED && !isOwner) {
            throw new ItemNotFoundException("Item not found with id: " + id);
        }

        LostItemResponseDTO dto = mapToResponseDTO(item, userId, isOwner);
        
        // Enrich with AI proposals if image exists
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            try {
                String localPath = toLocalImagePath(item.getImageUrl());
                
                List<java.util.Map<String, Object>> proposals = aiSimilarityClient.proposeSimilarItems(
                    id,
                    item.getType().name(),
                    item.getCategory(),
                    item.getTitle(),
                    item.getDescription(),
                    localPath,
                    5
                );
                
                // Enrich candidates with titles and image URLs from DB
                for (java.util.Map<String, Object> candidate : proposals) {
                    try {
                        Object cid = candidate.get("candidate_post_id");
                        Long candidateId = Long.valueOf(cid.toString());
                        repository.findById(candidateId).ifPresent(c -> {
                            String displayTitle = (c.getTitle() != null && !c.getTitle().isEmpty()) ? c.getTitle() : "Item #" + candidateId;
                            candidate.put("title", displayTitle);
                            candidate.put("imageUrl", c.getImageUrl());
                            log.debug("Enriched AI candidate {}: {}", candidateId, displayTitle);
                        });
                    } catch (Exception e) {
                        log.warn("Failed to enrich AI candidate", e);
                    }
                }
                
                dto.setAiProposals(proposals);
            } catch (Exception e) {
                log.warn("Failed to attach AI proposals to item response", e);
            }
        }
        
        return dto;
    }

    /**
     * Get all items with pagination
     */
    public Page<LostItemResponseDTO> getAllItems(Pageable pageable, Long userId) {
        log.info("Fetching all items with pagination");
        
        // Ensure newest items are shown first if no sorting is specified
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize(), 
                Sort.by(Sort.Direction.DESC, "id")
            );
        }

        Specification<LostItem> visibleItemsSpec = (root, query, cb) ->
                cb.notEqual(root.get("status"), LostItemStatus.BLOCKED);

        Page<LostItem> itemsPage = repository.findAll(visibleItemsSpec, pageable);
        return itemsPage.map(item -> mapToResponseDTO(item, userId, userId != null && item.getUserId().equals(userId)));
    }

        /**
         * Advanced search with dynamic filters
         */
        public Page<LostItemResponseDTO> advancedSearch(
            String keyword,
            LostItemType type,
            LostItemStatus status,
            String category,
            String location,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable,
            Long userId) {
        log.info("Advanced search - keyword: {}, type: {}, status: {}, category: {}, location: {}",
            keyword, type, status, category, location);

        Specification<LostItem> specification = Specification
            .where(LostItemSpecifications.keywordContains(keyword))
            .and(LostItemSpecifications.hasType(type))
            .and(LostItemSpecifications.hasStatus(status))
            .and(LostItemSpecifications.categoryContains(category))
            .and(LostItemSpecifications.locationContains(location))
            .and(LostItemSpecifications.dateTimeBetween(fromDate, toDate));

        Specification<LostItem> finalSpecification = specification
            .and((root, query, cb) -> cb.notEqual(root.get("status"), LostItemStatus.BLOCKED));

        Page<LostItem> itemsPage = repository.findAll(finalSpecification, pageable);
        return itemsPage.map(item -> mapToResponseDTO(item, userId, userId != null && item.getUserId().equals(userId)));
        }

    /**
     * Get items by current user
     */
    public List<LostItemResponseDTO> getUserItems(Long userId) {
        log.info("Fetching items for user {}", userId);
        return repository.findByUserId(userId).stream()
                .map(item -> mapToResponseDTO(item, userId, true))
                .collect(Collectors.toList());
    }

    /**
     * Update item (only owner can update)
     */
    @Transactional
    public LostItemResponseDTO updateItem(Long id, LostItemRequestDTO requestDTO, Long userId) {
        log.info("Updating item {} by user {}", id, userId);
        
        LostItem item = repository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + id));

        // Vérifier que l'utilisateur est bien le propriétaire
        if (!item.getUserId().equals(userId)) {
            log.warn("User {} attempted to update item {} owned by {}", userId, id, item.getUserId());
            throw new UnauthorizedAccessException("You can only modify your own items");
        }

        item.setTitle(requestDTO.getTitle());
        item.setDescription(requestDTO.getDescription());
        item.setType(requestDTO.getType());
        item.setCategory(requestDTO.getCategory());
        item.setLocation(requestDTO.getLocation());
        item.setContactInfo(requestDTO.getContactInfo());
        
        if (requestDTO.getImageUrl() != null && !requestDTO.getImageUrl().isEmpty()) {
            item.setImageUrl(requestDTO.getImageUrl().trim());
        }

        LostItem updatedItem = repository.save(item);
        log.info("Item {} updated successfully", id);
        return mapToResponseDTO(updatedItem, userId, true);
    }

    /**
     * Mark item as resolved (only owner can mark as resolved)
     */
    @Transactional
    public LostItemResponseDTO markAsResolved(Long id, Long userId) {
        log.info("Marking item {} as resolved by user {}", id, userId);
        
        LostItem item = repository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + id));

        if (!item.getUserId().equals(userId)) {
            log.warn("User {} attempted to mark item {} owned by {}", userId, id, item.getUserId());
            throw new UnauthorizedAccessException("You can only modify your own items");
        }

        item.setStatus(LostItemStatus.RESOLVED);
        LostItem updatedItem = repository.save(item);
        log.info("Item {} marked as resolved", id);
        return mapToResponseDTO(updatedItem, userId, true);
    }

    /**
     * Delete item (only owner can delete)
     */
    @Transactional
    public void deleteItem(Long id, Long userId) {
        log.info("Deleting item {} by user {}", id, userId);
        
        LostItem item = repository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + id));

        if (!item.getUserId().equals(userId)) {
            log.warn("User {} attempted to delete item {} owned by {}", userId, id, item.getUserId());
            throw new UnauthorizedAccessException("You can only delete your own items");
        }

        repository.delete(item);
        log.info("Item {} deleted successfully", id);
        
        // Dé-indexer depuis FAISS pour garder l'index cohérent
        try {
            String url = "http://localhost:8000/v1/object-similarity/index/" + id + "?post_type=" + item.getType().name();
            new org.springframework.web.client.RestTemplate().delete(url);
            log.info("Item {} de-indexed from AI service", id);
        } catch (Exception e) {
            log.warn("Could not de-index item {} from AI service: {}", id, e.getMessage());
        }
    }

    /**
     * Search items by type (LOST or FOUND)
     */
    public List<LostItemResponseDTO> searchByType(LostItemType type, Long userId) {
        log.info("Searching items by type: {}", type);
        return repository.findByType(type).stream()
                .filter(item -> item.getStatus() == LostItemStatus.ACTIVE)
                .map(item -> mapToResponseDTO(item, userId, userId != null && item.getUserId().equals(userId)))
                .collect(Collectors.toList());
    }

    /**
     * Filter items by category
     */
    public List<LostItemResponseDTO> filterByCategory(String category, Long userId) {
        log.info("Filtering items by category: {}", category);
        return repository.findByCategory(category).stream()
                .filter(item -> item.getStatus() == LostItemStatus.ACTIVE)
                .map(item -> mapToResponseDTO(item, userId, userId != null && item.getUserId().equals(userId)))
                .collect(Collectors.toList());
    }

    /**
     * Filter items by location
     */
    public List<LostItemResponseDTO> filterByLocation(String location, Long userId) {
        log.info("Filtering items by location: {}", location);
        return repository.findByLocation(location).stream()
                .filter(item -> item.getStatus() == LostItemStatus.ACTIVE)
                .map(item -> mapToResponseDTO(item, userId, userId != null && item.getUserId().equals(userId)))
                .collect(Collectors.toList());
    }

    /**
     * Map LostItem to LostItemResponseDTO
     */
    private LostItemResponseDTO mapToResponseDTO(LostItem item, Long userId, boolean isOwner) {
        String safeContactInfo = isOwner ? item.getContactInfo() : maskContactInfo(item.getContactInfo());

        return LostItemResponseDTO.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .type(item.getType())
                .category(item.getCategory())
                .location(item.getLocation())
                .dateTime(item.getDateTime())
                .contactInfo(safeContactInfo)
                .status(item.getStatus())
                .userId(item.getUserId())
                .imageUrl(item.getImageUrl() != null ? item.getImageUrl().trim() : null)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .version(item.getVersion())
                .isOwner(isOwner)
                .build();
    }

    private String maskContactInfo(String contactInfo) {
        if (contactInfo == null || contactInfo.isBlank()) {
            return "";
        }
        int visibleChars = Math.min(3, contactInfo.length());
        String prefix = contactInfo.substring(0, visibleChars);
        return prefix + "***";
    }

}
