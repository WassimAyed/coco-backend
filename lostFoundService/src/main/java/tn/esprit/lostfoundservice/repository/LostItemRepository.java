package tn.esprit.lostfoundservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import tn.esprit.lostfoundservice.entity.LostItem;
import tn.esprit.lostfoundservice.entity.LostItemStatus;
import tn.esprit.lostfoundservice.entity.LostItemType;

import java.util.List;
import java.util.Optional;

@Repository
public interface LostItemRepository extends JpaRepository<LostItem, Long>, JpaSpecificationExecutor<LostItem> {
    List<LostItem> findByUserId(Long userId);
    List<LostItem> findByType(LostItemType type);
    List<LostItem> findByStatus(LostItemStatus status);
    List<LostItem> findByCategory(String category);
    List<LostItem> findByLocation(String location);
    Optional<LostItem> findByIdAndStatus(Long id, LostItemStatus status);
}
