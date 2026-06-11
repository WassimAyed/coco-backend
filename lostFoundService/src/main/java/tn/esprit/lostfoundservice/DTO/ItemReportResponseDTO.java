package tn.esprit.lostfoundservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.lostfoundservice.entity.LostItemStatus;
import tn.esprit.lostfoundservice.entity.ReportStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemReportResponseDTO {

    private Long id;
    private Long itemId;
    private String itemTitle;
    private LostItemStatus itemStatus;
    private Long itemOwnerUserId;
    private Long reporterUserId;
    private String reason;
    private String details;
    private ReportStatus status;
    private String moderatorComment;
    private Long reviewedByUserId;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
