package tn.esprit.lostfoundservice.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemReportRequestDTO {

    @NotBlank(message = "Reason is required")
    @Size(max = 120, message = "Reason is too long")
    private String reason;

    @Size(max = 1500, message = "Details is too long")
    private String details;
}
