package tn.esprit.eventservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDTO {

    private Long id;

    @NotBlank(message = "Le nom complet est obligatoire")
    @Size(min = 2, max = 100)
    private String fullName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    private String email;

    @Pattern(regexp = "^\\d{8}$", message = "Numéro de téléphone invalide (8 chiffres)")
    private String phone;

    private LocalDateTime registrationDate;

    @NotNull(message = "L'ID de l'événement est obligatoire")
    private Long eventId;

    private Long userId;
}