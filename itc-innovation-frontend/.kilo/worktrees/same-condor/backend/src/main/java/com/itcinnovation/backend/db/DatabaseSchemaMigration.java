package com.itcinnovation.backend.db;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseSchemaMigration {

    @Bean
    ApplicationRunner ensureUserSchema(JdbcTemplate jdbcTemplate) {
        return args -> {
            if (!tableExists(jdbcTemplate, "users")) {
                return;
            }

            ensureColumn(jdbcTemplate, "first_name", "ALTER TABLE users ADD COLUMN first_name VARCHAR(80) NOT NULL DEFAULT ''");
            ensureColumn(jdbcTemplate, "last_name", "ALTER TABLE users ADD COLUMN last_name VARCHAR(80) NOT NULL DEFAULT ''");
            ensureColumn(jdbcTemplate, "phone", "ALTER TABLE users ADD COLUMN phone VARCHAR(30)");
            ensureColumn(jdbcTemplate, "department", "ALTER TABLE users ADD COLUMN department VARCHAR(100)");
            ensureColumn(jdbcTemplate, "role", "ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'EMPLOYEE'");
            ensureColumn(jdbcTemplate, "status", "ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'");

            jdbcTemplate.update("UPDATE users SET role = 'EMPLOYEE' WHERE role IS NULL");
            jdbcTemplate.update("UPDATE users SET status = 'ACTIVE' WHERE status IS NULL");
        };
    }

    private boolean tableExists(JdbcTemplate jdbcTemplate, String tableName) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?",
                    Integer.class,
                    tableName) > 0;
        } catch (Exception exception) {
            return false;
        }
    }

    private void ensureColumn(JdbcTemplate jdbcTemplate, String columnName, String alterStatement) {
        try {
            DatabaseMetaData metadata = jdbcTemplate.getDataSource().getConnection().getMetaData();
            try (ResultSet columns = metadata.getColumns(null, null, "users", columnName)) {
                if (columns.next()) {
                    return;
                }
            }
            jdbcTemplate.execute(alterStatement);
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to verify database schema for column " + columnName, exception);
        }
    }
}
