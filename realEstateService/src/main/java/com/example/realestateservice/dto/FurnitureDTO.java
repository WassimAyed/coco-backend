package com.example.realestateservice.dto;

import com.example.realestateservice.entity.enums.Condition;
import com.example.realestateservice.entity.enums.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FurnitureDTO {

    private Long id;

    @NotBlank
    private String title;

    @Size(max = 2000)
    private String description;

    @NotBlank
    private String category;

    private Condition condition;

    @NotNull
    @PositiveOrZero
    private Double price;

    @NotNull
    @Min(0)
    private Integer quantity;

    private Status status;

    @NotNull
    private Long sellerId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Valid
    private List<FurnitureImageDTO> images;

    @Valid
    private AddressDTO address;

    @Valid
    private List<ReviewDTO> reviews;
}
