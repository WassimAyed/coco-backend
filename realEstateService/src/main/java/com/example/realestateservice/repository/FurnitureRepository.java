package com.example.realestateservice.repository;

import com.example.realestateservice.entity.Furniture;
import com.example.realestateservice.entity.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FurnitureRepository extends JpaRepository<Furniture, Long> {
    List<Furniture> findBySellerId(Long sellerId);
    List<Furniture> findByStatus(Status status);
    List<Furniture> findByCategory(String category);
}
