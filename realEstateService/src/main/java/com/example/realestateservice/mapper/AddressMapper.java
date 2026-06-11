package com.example.realestateservice.mapper;

import com.example.realestateservice.dto.AddressDTO;
import com.example.realestateservice.entity.Address;

public class AddressMapper {

    public static AddressDTO toDto(Address entity) {
        if (entity == null) return null;
        return AddressDTO.builder()
                .id(entity.getId())
                .city(entity.getCity())
                .street(entity.getStreet())
                .universityZone(entity.getUniversityZone())
                .apartmentNumber(entity.getApartmentNumber())
                .build();
    }

    public static Address toEntity(AddressDTO dto) {
        if (dto == null) return null;
        return Address.builder()
                .id(dto.getId())
                .city(dto.getCity())
                .street(dto.getStreet())
                .universityZone(dto.getUniversityZone())
                .apartmentNumber(dto.getApartmentNumber())
                .build();
    }
}
