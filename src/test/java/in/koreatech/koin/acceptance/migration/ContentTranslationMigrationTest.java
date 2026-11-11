package in.koreatech.koin.acceptance.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class ContentTranslationMigrationTest {
    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.29")
        .withDatabaseName("content_translation_migration")
        .withUsername("test")
        .withPassword("test");

    @Test
    void 번역_테이블만_추가하고_기존_사용자_언어를_변경하지_않는다() throws Exception {
        Flyway.configure().dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
            .locations("classpath:db/migration").target("32").load().migrate();
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                INSERT INTO users (id, password, nickname, name, user_type, anonymous_nickname, preferred_language)
                VALUES (1, 'test-password', 'migration-user', '기존이름', 'GENERAL', 'test-anonymous', 'en-US')
                """);
        }

        Flyway flyway = Flyway.configure().dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
            .locations("classpath:db/migration").target("33").load();
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(1);
        flyway.validate();
        assertThat(flyway.migrate().migrationsExecuted).isZero();

        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            try (ResultSet result = statement.executeQuery("SELECT name, preferred_language FROM users WHERE id = 1")) {
                assertThat(result.next()).isTrue();
                assertThat(result.getString("name")).isEqualTo("기존이름");
                assertThat(result.getString("preferred_language")).isEqualTo("en-US");
            }
            try (ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM content_translations")) {
                assertThat(result.next()).isTrue();
                assertThat(result.getInt(1)).isZero();
            }
            try (ResultSet result = statement.executeQuery("""
                SELECT IS_NULLABLE FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'content_translations'
                  AND COLUMN_NAME IN ('cache_key', 'resource_type', 'resource_id', 'parent_id', 'room_id', 'expires_at')
                """)) {
                int columns = 0;
                while (result.next()) {
                    assertThat(result.getString(1)).isEqualTo("NO");
                    columns++;
                }
                assertThat(columns).isEqualTo(6);
            }
            try (ResultSet result = statement.executeQuery("""
                SELECT NON_UNIQUE FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'content_translations'
                  AND INDEX_NAME = 'uk_content_translations_cache_key'
                """)) {
                assertThat(result.next()).isTrue();
                assertThat(result.getInt(1)).isZero();
            }
            try (ResultSet result = statement.executeQuery("""
                SELECT COUNT(*) FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'content_translations'
                  AND INDEX_NAME = 'idx_content_translations_expires_at'
                """)) {
                assertThat(result.next()).isTrue();
                assertThat(result.getInt(1)).isEqualTo(1);
            }
        }
    }

    private Connection connect() throws Exception {
        return DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
    }
}
