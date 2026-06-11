package tn.esprit.lostfoundservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.lostfoundservice.DTO.ItemReportRequestDTO;
import tn.esprit.lostfoundservice.DTO.ItemReportResponseDTO;
import tn.esprit.lostfoundservice.DTO.ItemReportReviewDTO;
import tn.esprit.lostfoundservice.entity.ItemReport;
import tn.esprit.lostfoundservice.entity.LostItemStatus;
import tn.esprit.lostfoundservice.entity.ReportStatus;
import tn.esprit.lostfoundservice.exception.ItemNotFoundException;
import tn.esprit.lostfoundservice.exception.UnauthorizedAccessException;
import tn.esprit.lostfoundservice.repository.ItemReportRepository;
import tn.esprit.lostfoundservice.repository.LostItemRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemReportService {

    private final ItemReportRepository reportRepository;
    private final LostItemRepository lostItemRepository;

    @Transactional
    public ItemReportResponseDTO createReport(Long itemId, Long reporterUserId, ItemReportRequestDTO dto) {
        var item = lostItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + itemId));

        if (item.getUserId().equals(reporterUserId)) {
            throw new UnauthorizedAccessException("You cannot report your own item");
        }

        if (reportRepository.existsByItemIdAndReporterUserIdAndStatus(itemId, reporterUserId, ReportStatus.OPEN)) {
            throw new IllegalStateException("You already have an open report for this item");
        }

        ItemReport report = ItemReport.builder()
                .itemId(itemId)
                .reporterUserId(reporterUserId)
                .reason(dto.getReason().trim())
                .details(dto.getDetails())
                .status(ReportStatus.OPEN)
                .build();

        return map(reportRepository.save(report));
    }

    public List<ItemReportResponseDTO> getMyReports(Long userId) {
        return reportRepository.findByReporterUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::map).toList();
    }

    public List<ItemReportResponseDTO> getReportsForMyItems(Long ownerUserId) {
        return reportRepository.findByItemOwnerUserIdOrderByCreatedAtDesc(ownerUserId)
                .stream()
                .filter(report -> report.getStatus() != ReportStatus.OPEN)
                .map(this::map)
                .toList();
    }

    public List<ItemReportResponseDTO> getReportsByStatus(ReportStatus status) {
        if (status == null) {
            return reportRepository.findAll().stream().map(this::map).toList();
        }
        return reportRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream().map(this::map).toList();
    }

    @Transactional
    public ItemReportResponseDTO reviewReport(Long reportId, Long reviewerUserId, ItemReportReviewDTO dto) {
        ItemReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ItemNotFoundException("Report not found with id: " + reportId));

        if (dto.getStatus() == ReportStatus.OPEN) {
            throw new IllegalStateException("Review status cannot be OPEN");
        }

        report.setStatus(dto.getStatus());
        report.setModeratorComment(dto.getModeratorComment());
        report.setReviewedByUserId(reviewerUserId);
        report.setReviewedAt(LocalDateTime.now());

        if (dto.getStatus() == ReportStatus.ACTION_TAKEN) {
            lostItemRepository.findById(report.getItemId()).ifPresent(item -> {
                item.setStatus(LostItemStatus.BLOCKED);
                lostItemRepository.save(item);
            });
        }

        return map(reportRepository.save(report));
    }

    private ItemReportResponseDTO map(ItemReport report) {
        var item = lostItemRepository.findById(report.getItemId()).orElse(null);

        return ItemReportResponseDTO.builder()
                .id(report.getId())
                .itemId(report.getItemId())
                .itemTitle(item != null ? item.getTitle() : null)
                .itemStatus(item != null ? item.getStatus() : null)
                .itemOwnerUserId(item != null ? item.getUserId() : null)
                .reporterUserId(report.getReporterUserId())
                .reason(report.getReason())
                .details(report.getDetails())
                .status(report.getStatus())
                .moderatorComment(report.getModeratorComment())
                .reviewedByUserId(report.getReviewedByUserId())
                .reviewedAt(report.getReviewedAt())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
