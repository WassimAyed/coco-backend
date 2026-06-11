package tn.esprit.realestateservice.service;

import tn.esprit.realestateservice.entity.Furniture;

import java.util.List;

public interface FurnitureService {
    Furniture create(Furniture furniture);
    Furniture findById(Long id);
    List<Furniture> findAll();
    Furniture update(Long id, Furniture furniture);
    void delete(Long id);
}
