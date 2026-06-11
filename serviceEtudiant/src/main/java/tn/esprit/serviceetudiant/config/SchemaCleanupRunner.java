package tn.esprit.serviceetudiant.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class SchemaCleanupRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    private static final String[] ORPHANED_COLUMNS = {
            "availability", "response_time", "description", "rating", "review_count"
    };

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            jdbcTemplate.execute("DROP TABLE IF EXISTS `service_reviews`");
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            log.info("[SchemaCleanup] Dropped orphaned table 'service_reviews' if it existed.");
        } catch (Exception ex) {
            log.warn("[SchemaCleanup] Could not drop table 'service_reviews': {}", ex.getMessage());
        }
        for (String column : ORPHANED_COLUMNS) {
            try {
                jdbcTemplate.execute("ALTER TABLE student_services DROP COLUMN IF EXISTS `" + column + "`");
                log.info("[SchemaCleanup] Dropped orphaned column '{}' if it existed.", column);
            } catch (Exception ex) {
                log.warn("[SchemaCleanup] Could not drop column '{}': {}", column, ex.getMessage());
            }
        }
    }
}
