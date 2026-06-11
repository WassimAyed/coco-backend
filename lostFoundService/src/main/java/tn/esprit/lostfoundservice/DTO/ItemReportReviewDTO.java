package tn.esprit.lostfoundservice.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.lostfoundservice.entity.ReportStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemReportReviewDTO {

    @NotNull(message = "Status is required")
    private ReportStatus status;

    @Size(max = 1500, message = "Moderator comment is too long")
    private String moderatorComment;
}
