package com.example.realestateservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private Long id;

    @NotBlank
    private String city;

    @NotBlank
    private String street;

    private String universityZone;

    private String apartmentNumber;
}
