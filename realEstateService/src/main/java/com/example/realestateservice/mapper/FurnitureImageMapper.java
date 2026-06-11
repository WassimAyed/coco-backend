package com.example.realestateservice.mapper;

import com.example.realestateservice.dto.FurnitureImageDTO;
import com.example.realestateservice.entity.FurnitureImage;

public class FurnitureImageMapper {

    public static FurnitureImageDTO toDto(FurnitureImage entity) {
        if (entity == null) return null;
        return FurnitureImageDTO.builder()
                .id(entity.getId())
                .imageUrl(entity.getImageUrl())
                .build();
    }

    public static FurnitureImage toEntity(FurnitureImageDTO dto) {
        if (dto == null) return null;
        return FurnitureImage.builder()
                .id(dto.getId())
                .imageUrl(dto.getImageUrl())
                .build();
    }
}
