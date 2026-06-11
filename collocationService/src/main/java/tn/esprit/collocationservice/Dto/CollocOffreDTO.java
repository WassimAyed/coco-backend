package tn.esprit.collocationservice.Dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollocOffreDTO {
    private Long id;
    private String titre;
    private String description;
    private Double prixLoc;
    private String ville;
    private Integer chambres;
    private Boolean meublee;
    private Double latitude;
    private Double longitude;
    private LocalDate createdAt;
    private LocalDate expiryDate;
    private Long ownerId;
    private Boolean notified;
    private List<CollocOffreImageDTO> imagesColoc;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CollocOffreImageDTO {
        private Long id;
        private String filename;
        private String url;
    }
}
