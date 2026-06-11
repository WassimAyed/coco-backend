package tn.esprit.lostfoundservice.DTO;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemClaimDecisionDTO {

    @Size(max = 1000, message = "Comment is too long")
    private String comment;
}
