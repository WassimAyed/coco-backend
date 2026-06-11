package tn.esprit.realestateservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.realestateservice.dto.AddressDTO;
import tn.esprit.realestateservice.entity.Address;
import tn.esprit.realestateservice.entity.Furniture;
import tn.esprit.realestateservice.exception.ResourceNotFoundException;
import tn.esprit.realestateservice.repository.AddressRepository;
import tn.esprit.realestateservice.repository.FurnitureRepository;
import tn.esprit.realestateservice.service.AddressService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final FurnitureRepository furnitureRepository;

    @Override
    public AddressDTO create(AddressDTO dto) {
        Furniture furniture = furnitureRepository.findById(dto.getFurnitureId())
                .orElseThrow(() -> new ResourceNotFoundException("Furniture not found with id: " + dto.getFurnitureId()));
        Address entity = Address.builder()
                .id(dto.getId())
                .city(dto.getCity())
                .street(dto.getStreet())
                .universityZone(dto.getUniversityZone())
                .apartmentNumber(dto.getApartmentNumber())
                .furniture(furniture)
                .build();
        Address saved = addressRepository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDTO findById(Long id) {
        Address entity = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> findAll() {
        return addressRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO update(Long id, AddressDTO dto) {
        Address existing = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
        Furniture furniture = furnitureRepository.findById(dto.getFurnitureId())
                .orElseThrow(() -> new ResourceNotFoundException("Furniture not found with id: " + dto.getFurnitureId()));
        existing.setCity(dto.getCity());
        existing.setStreet(dto.getStreet());
        existing.setUniversityZone(dto.getUniversityZone());
        existing.setApartmentNumber(dto.getApartmentNumber());
        existing.setFurniture(furniture);
        Address saved = addressRepository.save(existing);
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        if (!addressRepository.existsById(id)) {
            throw new ResourceNotFoundException("Address not found with id: " + id);
        }
        addressRepository.deleteById(id);
    }

    private AddressDTO toDto(Address entity) {
        if (entity == null) {
            return null;
        }
        return AddressDTO.builder()
                .id(entity.getId())
                .city(entity.getCity())
                .street(entity.getStreet())
                .universityZone(entity.getUniversityZone())
                .apartmentNumber(entity.getApartmentNumber())
                .furnitureId(entity.getFurniture() != null ? entity.getFurniture().getId() : null)
                .build();
    }
}
