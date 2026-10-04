package in.koreatech.koin.acceptance.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.Flyway;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.model.DiningReport;
import in.koreatech.koin.domain.dining.model.DiningReportChange;
import in.koreatech.koin.domain.dining.model.DiningReportSequence;

@Testcontainers
class DiningSoldOutReportMigrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.29")
        .withDatabaseName("dining_soldout_report_migration")
        .withUsername("test")
        .withPassword("test");

    @Test
    void 기존_데이터에_V12를_적용하고_회원_삭제_후에도_제보와_처리이력을_보존한다() throws SQLException {
        migrateTo("11");
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                INSERT INTO users (id, password, user_type, anonymous_nickname)
                VALUES (101, 'test', 'STUDENT', '기존학생')
                """);
            statement.executeUpdate("INSERT INTO students (user_id, student_number) VALUES (101, '2025100101')");
            statement.executeUpdate("""
                INSERT INTO dining_menus (id, date, type, place, menu, sold_out)
                VALUES (201, '2026-10-02', 'LUNCH', 'A코너', '기존 메뉴', '2026-10-02 12:30:00')
                """);
        }
        migrateTo("12");
        // Flyway 결과를 실제 엔티티의 enum, UUID, sequence 매핑과 대조한다.
        try (SessionFactory ignored = new Configuration()
            .addAnnotatedClass(Dining.class)
            .addAnnotatedClass(DiningReport.class)
            .addAnnotatedClass(DiningReportChange.class)
            .addAnnotatedClass(DiningReportSequence.class)
            .setProperty("hibernate.connection.url", MYSQL.getJdbcUrl())
            .setProperty("hibernate.connection.username", MYSQL.getUsername())
            .setProperty("hibernate.connection.password", MYSQL.getPassword())
            .setProperty("hibernate.hbm2ddl.auto", "validate")
            .buildSessionFactory();
            Connection connection = getConnection();
            Statement statement = connection.createStatement()) {
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM dining_menus WHERE id = 201 AND menu = '기존 메뉴'
                    AND sold_out = '2026-10-02 12:30:00' AND sold_out_source IS NULL
                """)).isOne();
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM users u JOIN students s ON s.user_id = u.id
                WHERE u.id = 101 AND u.anonymous_nickname = '기존학생' AND s.student_number = '2025100101'
                """)).isOne();
            assertThat(queryInt(statement,
                "SELECT COUNT(*) FROM dining_soldout_report_sequence WHERE id = 1 AND last_sequence = 0")).isOne();

            statement.executeUpdate("""
                INSERT INTO dining_soldout_report
                    (id, reporter_id, dining_id, image_url, request_key, created_at, updated_at, status,
                     processing_type, processing_id, processed_at,
                     processor_workspace_id, processor_user_id, processor_name)
                VALUES (301, 101, 201, 'https://example.com/report.jpg', UNHEX(REPEAT('01', 16)),
                        '2026-10-02 12:35:00', '2026-10-02 12:40:00', 'APPROVED',
                        'MANUAL', UNHEX(REPEAT('02', 16)), '2026-10-02 12:40:00', 'workspace', 'processor', '담당자')
                """);
            statement.executeUpdate("""
                INSERT INTO dining_soldout_report_change
                    (sequence, report_id, event_type, status, processing_type, processing_id, occurred_at)
                VALUES (1, 301, 'PROCESSED', 'APPROVED', 'MANUAL',
                        UNHEX(REPEAT('02', 16)), '2026-10-02 12:40:00')
                """);
            statement.executeUpdate("UPDATE dining_soldout_report_sequence SET last_sequence = 1 WHERE id = 1");
            statement.executeUpdate("DELETE FROM users WHERE id = 101");

            assertThat(queryInt(statement, "SELECT COUNT(*) FROM users WHERE id = 101")).isZero();
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM dining_soldout_report WHERE id = 301 AND reporter_id IS NULL
                    AND dining_id = 201 AND status = 'APPROVED' AND processor_name = '담당자'
                    AND processing_type = 'MANUAL' AND processing_id = UNHEX(REPEAT('02', 16))
                """)).isOne();
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM dining_soldout_report_change WHERE report_id = 301 AND sequence = 1
                    AND event_type = 'PROCESSED' AND status = 'APPROVED' AND processing_type = 'MANUAL'
                    AND processing_id = UNHEX(REPEAT('02', 16))
                """)).isOne();
        }
    }

    private static void migrateTo(String version) {
        Flyway.configure()
            .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
            .locations("classpath:db/migration")
            .target(version)
            .load()
            .migrate();
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
    }

    private int queryInt(Statement statement, String query) throws SQLException {
        try (ResultSet result = statement.executeQuery(query)) {
            assertThat(result.next()).isTrue();
            return result.getInt(1);
        }
    }
}
