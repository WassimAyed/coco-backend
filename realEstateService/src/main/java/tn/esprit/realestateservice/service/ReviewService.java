package tn.esprit.realestateservice.service;

import tn.esprit.realestateservice.dto.ReviewDTO;

import java.util.List;

public interface ReviewService {
    ReviewDTO create(ReviewDTO dto);
    ReviewDTO findById(Long id);
    List<ReviewDTO> findAll();
    ReviewDTO update(Long id, ReviewDTO dto);
    void delete(Long id);
}
