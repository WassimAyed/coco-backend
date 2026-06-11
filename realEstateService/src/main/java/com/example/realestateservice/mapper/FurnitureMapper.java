package com.example.realestateservice.mapper;

import com.example.realestateservice.dto.FurnitureDTO;
import com.example.realestateservice.dto.FurnitureImageDTO;
import com.example.realestateservice.entity.Furniture;
import com.example.realestateservice.entity.FurnitureImage;

import java.util.List;
import java.util.stream.Collectors;

public class FurnitureMapper {

    public static FurnitureDTO toDto(Furniture entity) {
        if (entity == null) return null;
        List<FurnitureImageDTO> images = null;
        if (entity.getImages() != null) {
            images = entity.getImages().stream().map(FurnitureImageMapper::toDto).collect(Collectors.toList());
        }

        return FurnitureDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .condition(entity.getCondition())
                .price(entity.getPrice())
                .quantity(entity.getQuantity())
                .status(entity.getStatus())
                .sellerId(entity.getSellerId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .images(images)
                .address(AddressMapper.toDto(entity.getAddress()))
                .reviews(entity.getReviews() != null ? entity.getReviews().stream().map(ReviewMapper::toDto).collect(Collectors.toList()) : null)
                .build();
    }

    public static Furniture toEntity(FurnitureDTO dto) {
        if (dto == null) return null;
        Furniture furniture = Furniture.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .condition(dto.getCondition())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .status(dto.getStatus())
                .sellerId(dto.getSellerId())
                .build();

        if (dto.getAddress() != null) {
            furniture.setAddress(AddressMapper.toEntity(dto.getAddress()));
            if (furniture.getAddress() != null) furniture.getAddress().setFurniture(furniture);
        }

        if (dto.getImages() != null) {
            List<FurnitureImage> images = dto.getImages().stream().map(FurnitureImageMapper::toEntity).collect(Collectors.toList());
            images.forEach(img -> img.setFurniture(furniture));
            furniture.setImages(images);
        }

        if (dto.getReviews() != null) {
            List<com.example.realestateservice.entity.Review> reviews = dto.getReviews().stream().map(ReviewMapper::toEntity).collect(Collectors.toList());
            reviews.forEach(r -> r.setFurniture(furniture));
            furniture.setReviews(reviews);
        }

        return furniture;
    }
}
