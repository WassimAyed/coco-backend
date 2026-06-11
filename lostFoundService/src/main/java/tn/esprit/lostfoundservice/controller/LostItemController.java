package tn.esprit.lostfoundservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.lostfoundservice.DTO.LostItemRequestDTO;
import tn.esprit.lostfoundservice.DTO.LostItemResponseDTO;
import tn.esprit.lostfoundservice.entity.LostItemStatus;
import tn.esprit.lostfoundservice.entity.LostItemType;
import tn.esprit.lostfoundservice.exception.UnauthorizedAccessException;
import tn.esprit.lostfoundservice.service.AiSimilarityClient;
import tn.esprit.lostfoundservice.service.LostItemService;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class LostItemController {

    private final LostItemService lostItemService;
    private final AiSimilarityClient aiSimilarityClient;
    @Value("${lostfound.image.storage-path:./uploads/lostfound}")
    private String imageStoragePath;
    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    @Value("${lostfound.image.base-url:http://localhost:9092/api/lost-found/images}")
    private String imageBaseUrl;

    private Path resolveUploadDir() {
        return Paths.get(imageStoragePath).toAbsolutePath().normalize();
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new UnauthorizedAccessException("User identity is required");
        }
        return userId;
    }

    /**
     * Create a new lost/found item
     */
    @PostMapping
    public ResponseEntity<LostItemResponseDTO> createItem(
            @Valid @RequestBody LostItemRequestDTO requestDTO,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.info("POST /api/v1/items - Creating new item for user {}", userId);
        
        userId = requireUserId(userId);
        
        LostItemResponseDTO response = lostItemService.createItem(requestDTO, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all items with pagination
     */
    @GetMapping
    public ResponseEntity<Page<LostItemResponseDTO>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.info("GET /api/v1/items - Fetching all items (page: {}, size: {})", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LostItemResponseDTO> items = lostItemService.getAllItems(pageable, userId);
        return ResponseEntity.ok(items);
    }

    /**
     * Advanced search endpoint
     */
    @GetMapping("/search/advanced")
    public ResponseEntity<Page<LostItemResponseDTO>> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LostItemType type,
            @RequestParam(required = false) LostItemStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        log.info("GET /api/v1/items/search/advanced - keyword: {}, type: {}, status: {}", keyword, type, status);

        Sort sort = "asc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LostItemResponseDTO> result = lostItemService.advancedSearch(
                keyword, type, status, category, location, fromDate, toDate, pageable, userId
        );
        return ResponseEntity.ok(result);
    }

    /**
     * Get item by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<LostItemResponseDTO> getItemById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.info("GET /api/v1/items/{} - Fetching item", id);
        
        LostItemResponseDTO item = lostItemService.getItemById(id, userId);
        return ResponseEntity.ok(item);
    }

    /**
     * Get user's own items
     */
    @GetMapping("/user/my-items")
    public ResponseEntity<List<LostItemResponseDTO>> getUserItems(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.info("GET /api/v1/items/user/my-items - Fetching items for user {}", userId);
        
        userId = requireUserId(userId);
        
        List<LostItemResponseDTO> items = lostItemService.getUserItems(userId);
        return ResponseEntity.ok(items);
    }

    /**
     * Update item (only owner can update)
     */
    @PutMapping("/{id}")
    public ResponseEntity<LostItemResponseDTO> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody LostItemRequestDTO requestDTO,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.info("PUT /api/v1/items/{} - Updating item for user {}", id, userId);
        
        userId = requireUserId(userId);
        
        LostItemResponseDTO updatedItem = lostItemService.updateItem(id, requestDTO, userId);
        return ResponseEntity.ok(updatedItem);
    }

    /**
     * Mark item as resolved
     */
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<LostItemResponseDTO> markAsResolved(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.info("PATCH /api/v1/items/{}/resolve - Marking item as resolved for user {}", id, userId);
        
        userId = requireUserId(userId);
        
        LostItemResponseDTO resolvedItem = lostItemService.markAsResolved(id, userId);
        return ResponseEntity.ok(resolvedItem);
    }

    /**
     * Delete item (only owner can delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.info("DELETE /api/v1/items/{} - Deleting item for user {}", id, userId);
        
        userId = requireUserId(userId);
        
        lostItemService.deleteItem(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search items by type (LOST or FOUND)
     */
    @GetMapping("/search/type")
    public ResponseEntity<List<LostItemResponseDTO>> searchByType(
            @RequestParam LostItemType type,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.info("GET /api/v1/items/search/type - Searching by type: {}", type);
        
        List<LostItemResponseDTO> items = lostItemService.searchByType(type, userId);
        return ResponseEntity.ok(items);
    }

    /**
     * Filter items by category
     */
    @GetMapping("/filter/category")
    public ResponseEntity<List<LostItemResponseDTO>> filterByCategory(
            @RequestParam String category,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.info("GET /api/v1/items/filter/category - Filtering by category: {}", category);
        
        List<LostItemResponseDTO> items = lostItemService.filterByCategory(category, userId);
        return ResponseEntity.ok(items);
    }

    /**
     * Filter items by location
     */
    @GetMapping("/filter/location")
    public ResponseEntity<List<LostItemResponseDTO>> filterByLocation(
            @RequestParam String location,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.info("GET /api/v1/items/filter/location - Filtering by location: {}", location);
        
        List<LostItemResponseDTO> items = lostItemService.filterByLocation(location, userId);
        return ResponseEntity.ok(items);
    }

    @PostMapping(value = "/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            validateImage(file);
            Path uploadDir = resolveUploadDir();
            Files.createDirectories(uploadDir);

            String original = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
            String extension = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) {
                extension = original.substring(dot);
            }

            String fileName = UUID.randomUUID() + extension;
            Path target = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageBaseUrl + "/" + fileName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Unable to upload image", e);
        }
    }

    @GetMapping("/images/{fileName}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) throws MalformedURLException {
        Path uploadDir = resolveUploadDir();
        Path imagePath = uploadDir.resolve(fileName).normalize();
        if (!imagePath.startsWith(uploadDir)) {
            return ResponseEntity.badRequest().build();
        }
        Resource resource = new UrlResource(imagePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = "application/octet-stream";
        try {
            contentType = Files.probeContentType(imagePath);
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType == null ? "application/octet-stream" : contentType))
                .body(resource);
    }

    /**
     * Propose AI-based similar items for an existing post
     */
    @GetMapping("/{id}/ai/propose")
    public ResponseEntity<List<Map<String, Object>>> aiPropose(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int topK) {

        log.info("GET /api/v1/items/{}/ai/propose", id);
        LostItemResponseDTO item = lostItemService.getItemById(id, null);

        if (item.getImageUrl() == null || item.getImageUrl().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        String[] parts = item.getImageUrl().split("/");
        String fileName = parts[parts.length - 1];
        String localPath = resolveUploadDir().resolve(fileName).toString();

        List<Map<String, Object>> proposals = aiSimilarityClient.proposeSimilarItems(
            id,
            item.getType().name(),
            item.getCategory(),
            item.getTitle(),
            item.getDescription(),
            localPath,
            topK
        );
        return ResponseEntity.ok(proposals);
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException("Image too large. Max allowed size is 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Only JPG, PNG and WEBP images are allowed");
        }
    }
}
