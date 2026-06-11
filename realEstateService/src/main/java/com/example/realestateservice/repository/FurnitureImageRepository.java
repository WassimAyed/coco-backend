package com.example.realestateservice.repository;

import com.example.realestateservice.entity.FurnitureImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FurnitureImageRepository extends JpaRepository<FurnitureImage, Long> {
}
