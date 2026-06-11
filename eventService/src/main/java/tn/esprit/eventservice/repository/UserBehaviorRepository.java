package tn.esprit.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.eventservice.entity.UserBehavior;

import java.util.List;

@Repository
public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Long> {

    @Query("SELECT ub.categoryId, COUNT(ub) FROM UserBehavior ub "
            + "WHERE ub.userId = :userId "
            + "AND ub.actionType IN ('VIEW','PARTICIPATE','BOOKMARK','COMMENT',"
            + "'LIKE','LOVE','HAHA','WOW','RATE_4','RATE_5') "
            + "AND ub.categoryId IS NOT NULL "
            + "GROUP BY ub.categoryId ORDER BY COUNT(ub) DESC")
    List<Object[]> findCategoryFrequency(@Param("userId") Long userId);

    @Query("SELECT AVG(e.latitude), AVG(e.longitude) "
            + "FROM UserBehavior ub JOIN Event e ON e.id = ub.eventId "
            + "WHERE ub.userId = :userId "
            + "AND ub.actionType IN ('PARTICIPATE','RATE_4','RATE_5') "
            + "AND e.latitude IS NOT NULL AND e.longitude IS NOT NULL")
    Object[] findPreferredLocation(@Param("userId") Long userId);

    @Query("SELECT MIN(e.price), MAX(e.price) "
            + "FROM UserBehavior ub JOIN Event e ON e.id = ub.eventId "
            + "WHERE ub.userId = :userId "
            + "AND ub.actionType IN ('PARTICIPATE','RATE_4','RATE_5') "
            + "AND e.price IS NOT NULL")
    Object[] findPriceRange(@Param("userId") Long userId);

    @Query("SELECT e.eventType, COUNT(ub) "
            + "FROM UserBehavior ub JOIN Event e ON e.id = ub.eventId "
            + "WHERE ub.userId = :userId "
            + "AND ub.actionType IN ('VIEW','PARTICIPATE','BOOKMARK','COMMENT',"
            + "'LIKE','LOVE','HAHA','WOW','RATE_4','RATE_5') "
            + "GROUP BY e.eventType ORDER BY COUNT(ub) DESC")
    List<Object[]> findPreferredEventTypes(@Param("userId") Long userId);
}
