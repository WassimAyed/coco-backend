package tn.esprit.realestateservice.service;

import tn.esprit.realestateservice.dto.FurnitureImageDTO;

import java.util.List;

public interface FurnitureImageService {
    FurnitureImageDTO create(FurnitureImageDTO dto);
    FurnitureImageDTO findById(Long id);
    List<FurnitureImageDTO> findAll();
    FurnitureImageDTO update(Long id, FurnitureImageDTO dto);
    void delete(Long id);
}
