package com.example.realestateservice.mapper;

import com.example.realestateservice.dto.ReviewDTO;
import com.example.realestateservice.entity.Review;

public class ReviewMapper {

    public static ReviewDTO toDto(Review entity) {
        if (entity == null) return null;
        ReviewDTO dto = new ReviewDTO();
        dto.setId(entity.getId());
        dto.setRating(entity.getRating());
        dto.setComment(entity.getComment());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setReviewerId(entity.getReviewerId());
        return dto;
    }

    public static Review toEntity(ReviewDTO dto) {
        if (dto == null) return null;
        Review entity = new Review();
        entity.setId(dto.getId());
        entity.setRating(dto.getRating());
        entity.setComment(dto.getComment());
        entity.setReviewerId(dto.getReviewerId());
        return entity;
    }
}
