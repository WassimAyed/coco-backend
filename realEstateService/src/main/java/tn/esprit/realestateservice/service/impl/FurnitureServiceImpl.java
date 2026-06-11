package tn.esprit.realestateservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.realestateservice.entity.Furniture;
import tn.esprit.realestateservice.exception.ResourceNotFoundException;
import tn.esprit.realestateservice.repository.FurnitureRepository;
import tn.esprit.realestateservice.service.FurnitureService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FurnitureServiceImpl implements FurnitureService {

    private final FurnitureRepository furnitureRepository;

    @Override
    public Furniture create(Furniture furniture) {
        return furnitureRepository.save(furniture);
    }

    @Override
    @Transactional(readOnly = true)
    public Furniture findById(Long id) {
        return furnitureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Furniture not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Furniture> findAll() {
        return furnitureRepository.findAll();
    }

    @Override
    public Furniture update(Long id, Furniture furniture) {
        Furniture existing = furnitureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Furniture not found with id: " + id));
        existing.setTitle(furniture.getTitle());
        existing.setDescription(furniture.getDescription());
        existing.setCategory(furniture.getCategory());
        existing.setCondition(furniture.getCondition());
        existing.setPrice(furniture.getPrice());
        existing.setQuantity(furniture.getQuantity());
        existing.setStatus(furniture.getStatus());
        existing.setSellerId(furniture.getSellerId());
        return furnitureRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!furnitureRepository.existsById(id)) {
            throw new ResourceNotFoundException("Furniture not found with id: " + id);
        }
        furnitureRepository.deleteById(id);
    }
}
