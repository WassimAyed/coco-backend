package tn.esprit.serviceetudiant.dto;

public record CoverUploadResponse(
        String objectKey,
        String imageUrl,
        String fileName,
        long size,
        String mimeType
) {
}