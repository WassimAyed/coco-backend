package tn.esprit.collocationservice.Dto;

import lombok.*;
import tn.esprit.collocationservice.Entity.collocOffreRequest.Status;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollocOffreRequestDTO {
    private Long id;
    private Long studentId;
    private Long offerId;
    private String offerTitle;
    private Status status;
    private LocalDateTime createdAt;
}
