package tn.esprit.lostfoundservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.lostfoundservice.entity.ItemReport;
import tn.esprit.lostfoundservice.entity.ReportStatus;

import java.util.List;

@Repository
public interface ItemReportRepository extends JpaRepository<ItemReport, Long> {
    List<ItemReport> findByReporterUserIdOrderByCreatedAtDesc(Long reporterUserId);
    List<ItemReport> findByItemIdOrderByCreatedAtDesc(Long itemId);
    List<ItemReport> findByStatusOrderByCreatedAtDesc(ReportStatus status);
    boolean existsByItemIdAndReporterUserIdAndStatus(Long itemId, Long reporterUserId, ReportStatus status);

    @Query("SELECT r FROM ItemReport r WHERE r.itemId IN (SELECT i.id FROM LostItem i WHERE i.userId = :ownerUserId) ORDER BY r.createdAt DESC")
    List<ItemReport> findByItemOwnerUserIdOrderByCreatedAtDesc(@Param("ownerUserId") Long ownerUserId);
}
