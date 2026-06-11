package tn.esprit.lostfoundservice.specification;

import org.springframework.data.jpa.domain.Specification;
import tn.esprit.lostfoundservice.entity.LostItem;
import tn.esprit.lostfoundservice.entity.LostItemStatus;
import tn.esprit.lostfoundservice.entity.LostItemType;

import java.time.LocalDateTime;

public final class LostItemSpecifications {

    private LostItemSpecifications() {
    }

    public static Specification<LostItem> hasType(LostItemType type) {
        return (root, query, cb) -> type == null ? cb.conjunction() : cb.equal(root.get("type"), type);
    }

    public static Specification<LostItem> hasStatus(LostItemStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<LostItem> categoryContains(String category) {
        return (root, query, cb) -> {
            if (category == null || category.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("category")), "%" + category.toLowerCase() + "%");
        };
    }

    public static Specification<LostItem> locationContains(String location) {
        return (root, query, cb) -> {
            if (location == null || location.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
        };
    }

    public static Specification<LostItem> keywordContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("category")), pattern),
                    cb.like(cb.lower(root.get("location")), pattern)
            );
        };
    }

    public static Specification<LostItem> dateTimeBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) {
                return cb.conjunction();
            }
            if (from != null && to != null) {
                return cb.between(root.get("dateTime"), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("dateTime"), from);
            }
            return cb.lessThanOrEqualTo(root.get("dateTime"), to);
        };
    }
}
