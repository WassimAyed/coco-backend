package com.example.realestateservice.service;

import com.example.realestateservice.dto.FurnitureDTO;
import com.example.realestateservice.entity.enums.Status;

import java.util.List;

public interface FurnitureService {
    FurnitureDTO create(FurnitureDTO dto);
    FurnitureDTO findById(Long id);
    List<FurnitureDTO> findAll();
    FurnitureDTO update(Long id, FurnitureDTO dto);
    void delete(Long id);

    List<FurnitureDTO> findBySellerId(Long sellerId);
    List<FurnitureDTO> findByStatus(Status status);
    List<FurnitureDTO> findByCategory(String category);
}
