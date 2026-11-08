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
class UserLanguageMigrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.29")
        .withDatabaseName("user_language_migration")
        .withUsername("test")
        .withPassword("test");

    @Test
    void 새_언어_컬럼은_기존_계정값을_유지하고_미설정으로_추가된다() throws Exception {
        Flyway.configure().dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
            .locations("classpath:db/migration").target("28").load().migrate();

        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                INSERT INTO users (id, password, nickname, name, user_type, anonymous_nickname)
                VALUES (1, 'test-password', 'migration-user', '기존이름', 'GENERAL', 'test-anonymous')
                """);
        }

        Flyway flyway = Flyway.configure()
            .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
            .locations("classpath:db/migration").target("29").load();
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(1);
        flyway.validate();

        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            try (ResultSet result = statement.executeQuery("""
                SELECT password, nickname, name, user_type, preferred_language FROM users WHERE id = 1
                """)) {
                assertThat(result.next()).isTrue();
                assertThat(result.getString("password")).isEqualTo("test-password");
                assertThat(result.getString("nickname")).isEqualTo("migration-user");
                assertThat(result.getString("name")).isEqualTo("기존이름");
                assertThat(result.getString("user_type")).isEqualTo("GENERAL");
                assertThat(result.getString("preferred_language")).isNull();
            }
            try (ResultSet column = statement.executeQuery("""
                SELECT IS_NULLABLE, CHARACTER_MAXIMUM_LENGTH, COLUMN_DEFAULT
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'preferred_language'
                """)) {
                assertThat(column.next()).isTrue();
                assertThat(column.getString("IS_NULLABLE")).isEqualTo("YES");
                assertThat(column.getInt("CHARACTER_MAXIMUM_LENGTH")).isEqualTo(35);
                assertThat(column.getString("COLUMN_DEFAULT")).isNull();
            }
            statement.executeUpdate("UPDATE users SET preferred_language = 'en-US' WHERE id = 1");
        }

        assertThat(flyway.migrate().migrationsExecuted).isZero();
        try (Connection connection = connect(); Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT preferred_language FROM users WHERE id = 1")) {
            assertThat(result.next()).isTrue();
            assertThat(result.getString(1)).isEqualTo("en-US");
        }
    }

    private Connection connect() throws Exception {
        return DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
    }
}
