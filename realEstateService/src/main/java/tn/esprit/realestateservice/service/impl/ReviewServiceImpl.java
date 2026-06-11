package tn.esprit.realestateservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.realestateservice.dto.ReviewDTO;
import tn.esprit.realestateservice.entity.Furniture;
import tn.esprit.realestateservice.entity.Review;
import tn.esprit.realestateservice.exception.ResourceNotFoundException;
import tn.esprit.realestateservice.repository.FurnitureRepository;
import tn.esprit.realestateservice.repository.ReviewRepository;
import tn.esprit.realestateservice.service.ReviewService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final FurnitureRepository furnitureRepository;

    @Override
    public ReviewDTO create(ReviewDTO dto) {
        Furniture furniture = furnitureRepository.findById(dto.getFurnitureId())
                .orElseThrow(() -> new ResourceNotFoundException("Furniture not found with id: " + dto.getFurnitureId()));
        Review entity = Review.builder()
                .id(dto.getId())
                .rating(dto.getRating())
                .comment(dto.getComment())
                .reviewerId(dto.getReviewerId())
                .furniture(furniture)
                .build();
        Review saved = reviewRepository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDTO findById(Long id) {
        Review entity = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> findAll() {
        return reviewRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewDTO update(Long id, ReviewDTO dto) {
        Review existing = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        Furniture furniture = furnitureRepository.findById(dto.getFurnitureId())
                .orElseThrow(() -> new ResourceNotFoundException("Furniture not found with id: " + dto.getFurnitureId()));
        existing.setRating(dto.getRating());
        existing.setComment(dto.getComment());
        existing.setReviewerId(dto.getReviewerId());
        existing.setFurniture(furniture);
        Review saved = reviewRepository.save(existing);
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }

    private ReviewDTO toDto(Review entity) {
        if (entity == null) {
            return null;
        }
        return ReviewDTO.builder()
                .id(entity.getId())
                .rating(entity.getRating())
                .comment(entity.getComment())
                .createdAt(entity.getCreatedAt())
                .reviewerId(entity.getReviewerId())
                .furnitureId(entity.getFurniture() != null ? entity.getFurniture().getId() : null)
                .build();
    }
}
