package in.koreatech.koin.acceptance.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;

class NotificationFcmMigrationTest {

    @Test
    void 운영_baseline_누락_스키마와_신규_스키마에서_FCM_컬럼을_보장한다() throws SQLException {
        try (MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.29")
            .withDatabaseName("notification_fcm_migration")
            .withUsername("test")
            .withPassword("test")) {
            mysql.start();

            createProductionLikeNotificationTable(mysql);

            Flyway baselineRepairFlyway = flywayConfiguration(mysql)
                .target("1")
                .load();
            baselineRepairFlyway.migrate();
            baselineRepairFlyway.migrate();

            assertFcmColumnsAndHistory(mysql, "BASELINE");

            baselineRepairFlyway.clean();

            Flyway fullFlyway = flywayConfiguration(mysql).load();
            fullFlyway.migrate();
            fullFlyway.migrate();

            assertFcmColumnsAndHistory(mysql, "SQL");
        }
    }

    private FluentConfiguration flywayConfiguration(MySQLContainer<?> mysql) {
        return Flyway.configure()
            .dataSource(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword())
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .cleanDisabled(false);
    }

    private void createProductionLikeNotificationTable(MySQLContainer<?> mysql) throws SQLException {
        try (Connection connection = connect(mysql); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE notification (id BIGINT NOT NULL PRIMARY KEY) ENGINE=InnoDB");
        }
    }

    private void assertFcmColumnsAndHistory(MySQLContainer<?> mysql, String versionOneType) throws SQLException {
        try (Connection connection = connect(mysql)) {
            assertThat(queryInt(connection, """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'notification'
                  AND (
                      (COLUMN_NAME = 'is_push_success'
                          AND COLUMN_TYPE = 'tinyint(1)'
                          AND IS_NULLABLE = 'YES')
                      OR (COLUMN_NAME IN ('fcm_error_code', 'fcm_messaging_error_code')
                          AND DATA_TYPE = 'varchar'
                          AND CHARACTER_MAXIMUM_LENGTH = 100
                          AND IS_NULLABLE = 'YES')
                  )
                """)).isEqualTo(3);
            assertThat(queryInt(connection, """
                SELECT COUNT(*)
                FROM flyway_schema_history
                WHERE version = '1'
                  AND type = '%s'
                  AND success = 1
                """.formatted(versionOneType))).isEqualTo(1);
            assertThat(queryInt(connection, """
                SELECT COUNT(*)
                FROM flyway_schema_history
                WHERE version IS NULL
                  AND type = 'SQL'
                  AND script = 'R__ensure_notification_fcm_columns.sql'
                  AND success = 1
                """)).isEqualTo(1);
        }
    }

    private Connection connect(MySQLContainer<?> mysql) throws SQLException {
        return DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
    }

    private int queryInt(Connection connection, String query) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(query)) {
            result.next();
            return result.getInt(1);
        }
    }
}
