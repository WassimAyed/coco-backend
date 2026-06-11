package tn.esprit.lostfoundservice.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.lostfoundservice.entity.LostItemType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LostItemRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Type is required (LOST or FOUND)")
    private LostItemType type;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Contact info is required")
    private String contactInfo;

    private String imageUrl;
}
