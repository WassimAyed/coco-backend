package tn.esprit.eventservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.eventservice.entity.ReactionType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionDTO {

    private Long id;

    @NotNull(message = "Le type de réaction est obligatoire")
    private ReactionType type;

    @NotBlank(message = "Le nom de l'auteur est obligatoire")
    private String authorName;

    @NotBlank(message = "L'email est obligatoire")
    private String authorEmail;

    @NotNull(message = "L'ID de l'événement est obligatoire")
    private Long eventId;
}