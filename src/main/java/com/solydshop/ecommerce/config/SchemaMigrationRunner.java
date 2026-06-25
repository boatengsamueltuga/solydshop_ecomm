package com.solydshop.ecommerce.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Runs idempotent schema patches after Hibernate DDL completes.
 *
 * Used because ddl-auto=update keeps existing constraints as-is.
 * When the OrderStatus enum gains new values, the old orders_status_check
 * constraint in PostgreSQL rejects INSERT statements with those new values.
 */
@Component
public class SchemaMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaMigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public SchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        dropStaleOrdersStatusCheck();
    }

    private void dropStaleOrdersStatusCheck() {
        try {
            jdbcTemplate.execute("ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check");
            log.info("SchemaMigration: orders_status_check constraint removed (or was already absent)");
        } catch (Exception e) {
            log.warn("SchemaMigration: could not drop orders_status_check — {}", e.getMessage());
        }
    }
}
