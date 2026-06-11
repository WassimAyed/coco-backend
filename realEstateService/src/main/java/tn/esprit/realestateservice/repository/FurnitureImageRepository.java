package tn.esprit.realestateservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.realestateservice.entity.FurnitureImage;

@Repository
public interface FurnitureImageRepository extends JpaRepository<FurnitureImage, Long> {
}
