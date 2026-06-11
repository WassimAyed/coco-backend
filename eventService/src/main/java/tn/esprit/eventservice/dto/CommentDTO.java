package tn.esprit.eventservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    private Long id;

    @NotBlank(message = "Le contenu est obligatoire")
    @Size(max = 1000, message = "Le commentaire ne doit pas dépasser 1000 caractères")
    private String content;

    @NotBlank(message = "Le nom de l'auteur est obligatoire")
    private String authorName;

    @NotBlank(message = "L'email de l'auteur est obligatoire")
    private String authorEmail;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @NotNull(message = "L'ID de l'événement est obligatoire")
    private Long eventId;
}