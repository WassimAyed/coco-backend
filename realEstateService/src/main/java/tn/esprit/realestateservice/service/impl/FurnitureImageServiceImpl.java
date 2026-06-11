package tn.esprit.realestateservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.realestateservice.dto.FurnitureImageDTO;
import tn.esprit.realestateservice.entity.Furniture;
import tn.esprit.realestateservice.entity.FurnitureImage;
import tn.esprit.realestateservice.exception.ResourceNotFoundException;
import tn.esprit.realestateservice.repository.FurnitureImageRepository;
import tn.esprit.realestateservice.repository.FurnitureRepository;
import tn.esprit.realestateservice.service.FurnitureImageService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FurnitureImageServiceImpl implements FurnitureImageService {

    private final FurnitureImageRepository furnitureImageRepository;
    private final FurnitureRepository furnitureRepository;

    @Override
    public FurnitureImageDTO create(FurnitureImageDTO dto) {
        Furniture furniture = furnitureRepository.findById(dto.getFurnitureId())
                .orElseThrow(() -> new ResourceNotFoundException("Furniture not found with id: " + dto.getFurnitureId()));
        FurnitureImage entity = FurnitureImage.builder()
                .id(dto.getId())
                .imageUrl(dto.getImageUrl())
                .furniture(furniture)
                .build();
        FurnitureImage saved = furnitureImageRepository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FurnitureImageDTO findById(Long id) {
        FurnitureImage entity = furnitureImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Furniture image not found with id: " + id));
        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FurnitureImageDTO> findAll() {
        return furnitureImageRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public FurnitureImageDTO update(Long id, FurnitureImageDTO dto) {
        FurnitureImage existing = furnitureImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Furniture image not found with id: " + id));
        Furniture furniture = furnitureRepository.findById(dto.getFurnitureId())
                .orElseThrow(() -> new ResourceNotFoundException("Furniture not found with id: " + dto.getFurnitureId()));
        existing.setImageUrl(dto.getImageUrl());
        existing.setFurniture(furniture);
        FurnitureImage saved = furnitureImageRepository.save(existing);
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        if (!furnitureImageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Furniture image not found with id: " + id);
        }
        furnitureImageRepository.deleteById(id);
    }

    private FurnitureImageDTO toDto(FurnitureImage entity) {
        if (entity == null) {
            return null;
        }
        return FurnitureImageDTO.builder()
                .id(entity.getId())
                .imageUrl(entity.getImageUrl())
                .furnitureId(entity.getFurniture() != null ? entity.getFurniture().getId() : null)
                .build();
    }
}
