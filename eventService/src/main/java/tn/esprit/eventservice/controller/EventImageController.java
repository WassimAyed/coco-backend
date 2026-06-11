package tn.esprit.eventservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventGallery;
import tn.esprit.eventservice.exception.ResourceNotFoundException;
import tn.esprit.eventservice.repository.EventGalleryRepository;
import tn.esprit.eventservice.repository.EventRepository;
import tn.esprit.eventservice.service.CloudinaryService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventImageController {

    private final CloudinaryService cloudinaryService;
    private final EventRepository eventRepository;
    private final EventGalleryRepository galleryRepository;

    public EventImageController(CloudinaryService cloudinaryService,
                                EventRepository eventRepository,
                                EventGalleryRepository galleryRepository) {
        this.cloudinaryService = cloudinaryService;
        this.eventRepository = eventRepository;
        this.galleryRepository = galleryRepository;
    }

    // ── Image principale ──
    @Operation(summary = "Upload image principale de l'événement")
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadMainImage(
            @PathVariable("id") Long id,
            @RequestPart("file") MultipartFile file) throws IOException {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable : " + id));

        String url = cloudinaryService.uploadImage(file, "events/main");
        event.setImageUrl(url);
        eventRepository.save(event);

        return ResponseEntity.ok(url);
    }

    // ── Galerie — ajouter une image ──
    @Operation(summary = "Ajouter une image à la galerie de l'événement")
    @PostMapping(value = "/{id}/gallery", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventGallery> addToGallery(
            @PathVariable("id") Long id,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption) throws IOException {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable : " + id));

        String url = cloudinaryService.uploadImage(file, "events/gallery");

        EventGallery gallery = new EventGallery();
        gallery.setImageUrl(url);
        gallery.setCaption(caption);
        gallery.setEvent(event);

        return ResponseEntity.ok(galleryRepository.save(gallery));
    }

    // ── Galerie — récupérer toutes les images ──
    @Operation(summary = "Récupérer la galerie d'un événement")
    @GetMapping("/{id}/gallery")
    public ResponseEntity<List<EventGallery>> getGallery(@PathVariable("id") Long id) {
        return ResponseEntity.ok(galleryRepository.findByEventId(id));
    }

    // ── Galerie — supprimer une image ──
    @Operation(summary = "Supprimer une image de la galerie")
    @DeleteMapping("/gallery/{imageId}")
    public ResponseEntity<Void> deleteFromGallery(@PathVariable("imageId") Long imageId) {
        galleryRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image introuvable : " + imageId));
        galleryRepository.deleteById(imageId);
        return ResponseEntity.noContent().build();
    }
}