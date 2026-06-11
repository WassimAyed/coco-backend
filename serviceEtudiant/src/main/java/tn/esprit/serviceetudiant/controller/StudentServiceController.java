package tn.esprit.serviceetudiant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.serviceetudiant.dto.CoverUploadResponse;
import tn.esprit.serviceetudiant.dto.ServiceModerationUpdateRequest;
import tn.esprit.serviceetudiant.dto.ServiceTagsUpdateRequest;
import tn.esprit.serviceetudiant.dto.StudentServiceResponse;
import tn.esprit.serviceetudiant.dto.StudentServiceUpsertRequest;
import tn.esprit.serviceetudiant.service.StudentServiceService;

import java.util.List;

@RestController
@RequestMapping("/student-services")
@RequiredArgsConstructor
public class StudentServiceController {

    private final StudentServiceService studentServiceService;

    @GetMapping
    public ResponseEntity<List<StudentServiceResponse>> getServices(@RequestParam(required = false) String search, @RequestParam(required = false) String category, @RequestParam(required = false) String deliveryMode, @RequestParam(required = false) Boolean featuredOnly) {
        return ResponseEntity.ok(studentServiceService.getServices(search, category, deliveryMode, featuredOnly));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<StudentServiceResponse>> getAllServicesForAdmin(@RequestParam(required = false) String search, @RequestParam(required = false) String category, @RequestParam(required = false) String deliveryMode, @RequestParam(required = false) String moderationStatus, @RequestParam(required = false) Boolean featuredOnly) {
        return ResponseEntity.ok(studentServiceService.getAdminServices(search, category, deliveryMode, moderationStatus, featuredOnly));
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<StudentServiceResponse>> getProviderServices(@PathVariable Long providerId) {
        return ResponseEntity.ok(studentServiceService.getServicesByProvider(providerId));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<StudentServiceResponse> getServiceBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(studentServiceService.getServiceBySlug(slug));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentServiceResponse> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(studentServiceService.getServiceById(id));
    }

    @PostMapping
    public ResponseEntity<StudentServiceResponse> createService(@Valid @RequestBody StudentServiceUpsertRequest request) {
        return ResponseEntity.ok(studentServiceService.createService(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentServiceResponse> updateService(@PathVariable Long id, @Valid @RequestBody StudentServiceUpsertRequest request) {
        return ResponseEntity.ok(studentServiceService.updateService(id, request));
    }

    @PutMapping("/admin/{id}/moderation")
    public ResponseEntity<StudentServiceResponse> moderateService(@PathVariable Long id, @Valid @RequestBody ServiceModerationUpdateRequest request) {
        return ResponseEntity.ok(studentServiceService.moderateService(id, request.status()));
    }

    @PutMapping("/admin/{id}/tags")
    public ResponseEntity<StudentServiceResponse> updateServiceTags(@PathVariable Long id, @Valid @RequestBody ServiceTagsUpdateRequest request) {
        return ResponseEntity.ok(studentServiceService.updateServiceTags(id, request.tags()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        studentServiceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload-cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CoverUploadResponse> uploadCoverImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "ownerId", required = false) Long ownerId
    ) {
        return ResponseEntity.ok(studentServiceService.uploadCoverImage(file, ownerId));
    }
}
