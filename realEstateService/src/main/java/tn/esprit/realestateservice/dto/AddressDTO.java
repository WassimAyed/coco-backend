package tn.esprit.realestateservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
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

    @NotNull
    private Long furnitureId;
}
