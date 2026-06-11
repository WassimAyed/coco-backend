package tn.esprit.lostfoundservice.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemClaimRequestDTO {

    @NotBlank(message = "Proof message is required")
    private String proofMessage;
}
