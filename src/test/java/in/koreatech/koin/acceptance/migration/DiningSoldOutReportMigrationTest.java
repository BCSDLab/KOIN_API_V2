package in.koreatech.koin.acceptance.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import in.koreatech.koin.domain.dining.model.DiningReportDelivery;
import in.koreatech.koin.domain.dining.model.DiningReportDeliveryAttempt;
import in.koreatech.koin.domain.dining.model.DiningReportDeliveryTarget;
import in.koreatech.koin.domain.dining.model.DiningReportSequence;

@Testcontainers
class DiningSoldOutReportMigrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.29")
        .withDatabaseName("dining_soldout_report_migration")
        .withUsername("test")
        .withPassword("test");

    @Test
    void V11부터_V13까지_기존_이력을_보존하고_전송_스키마와_제약을_검증한다() throws SQLException {
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
        // V13 이전 제보는 메시지 연결을 알 수 없으므로 자동 CREATE 대상으로 이관하지 않는다.
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
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
        }
        migrateTo("13");
        // Flyway 결과를 실제 엔티티의 enum, UUID, sequence 매핑과 대조한다.
        try (SessionFactory ignored = new Configuration()
            .addAnnotatedClass(Dining.class)
            .addAnnotatedClass(DiningReport.class)
            .addAnnotatedClass(DiningReportChange.class)
            .addAnnotatedClass(DiningReportSequence.class)
            .addAnnotatedClass(DiningReportDeliveryTarget.class)
            .addAnnotatedClass(DiningReportDelivery.class)
            .addAnnotatedClass(DiningReportDeliveryAttempt.class)
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
                """
                    SELECT COUNT(*) FROM dining_soldout_report_sequence
                    WHERE id = 1 AND last_sequence = 1 AND delivery_cooldown_until IS NULL
                    """)).isOne();
            assertThat(queryInt(statement, "SELECT COUNT(*) FROM dining_soldout_report_delivery_target")).isZero();
            assertThat(queryInt(statement, "SELECT COUNT(*) FROM dining_soldout_report_delivery")).isZero();
            assertThat(queryInt(statement, "SELECT COUNT(*) FROM dining_soldout_report_delivery_attempt")).isZero();

            statement.executeUpdate("""
                INSERT INTO dining_soldout_report
                    (id, dining_id, image_url, request_key, created_at, updated_at)
                VALUES (302, 201, 'https://example.com/new.jpg', UNHEX(REPEAT('03', 16)),
                    '2026-10-02 12:45:00', '2026-10-02 12:45:00')
                """);
            statement.executeUpdate("""
                INSERT INTO dining_soldout_report_change
                    (sequence, report_id, event_type, status, occurred_at)
                VALUES (2, 302, 'CREATED', 'PENDING', '2026-10-02 12:45:00')
                """);
            statement.executeUpdate("UPDATE dining_soldout_report_sequence SET last_sequence = 2 WHERE id = 1");
            statement.executeUpdate("""
                INSERT INTO dining_soldout_report_delivery_target
                    (report_id, desired_sequence, desired_snapshot, workspace_id, channel_id, created_at, updated_at)
                VALUES (302, 2, '{"report_id":302,"status":"PENDING"}', 'T_EXAMPLE', 'C_EXAMPLE',
                    '2026-10-02 12:45:00.123456', '2026-10-02 12:45:00.123456')
                """);
            statement.executeUpdate("""
                INSERT INTO dining_soldout_report_delivery
                    (id, report_id, source_sequence, report_snapshot, operation, workspace_id, channel_id,
                     status, next_attempt_at, created_at, updated_at)
                VALUES (UNHEX(REPEAT('04', 16)), 302, 2, '{"report_id":302,"status":"PENDING"}',
                    'CREATE', 'T_EXAMPLE', 'C_EXAMPLE', 'QUEUED', '2026-10-02 12:45:00.123456',
                    '2026-10-02 12:45:00.123456', '2026-10-02 12:45:00.123456')
                """);
            statement.executeUpdate("""
                INSERT INTO dining_soldout_report_delivery_attempt
                    (token, delivery_id, mode, send_attempt_token, issued_at, expires_at, evidence_history)
                VALUES (UNHEX(REPEAT('05', 16)), UNHEX(REPEAT('04', 16)), 'SEND', UNHEX(REPEAT('05', 16)),
                    '2026-10-02 12:45:00.123456', '2026-10-02 12:46:00.123456', '[]')
                """);
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM dining_soldout_report_delivery_attempt
                WHERE expires_at = DATE_ADD(issued_at, INTERVAL 60 SECOND) AND MICROSECOND(issued_at) = 123456
                """)).isOne();
            assertThatThrownBy(() -> statement.executeUpdate("""
                INSERT INTO dining_soldout_report_delivery
                    (id, report_id, source_sequence, report_snapshot, operation, workspace_id, channel_id,
                     status, created_at, updated_at)
                SELECT UNHEX(REPEAT('06', 16)), report_id, source_sequence, report_snapshot, operation,
                    workspace_id, channel_id, status, created_at, updated_at
                FROM dining_soldout_report_delivery
                """)).isInstanceOf(SQLException.class).hasMessageContaining("uk_dining_delivery_snapshot");
            assertThatThrownBy(() -> statement.executeUpdate("""
                UPDATE dining_soldout_report_delivery_attempt SET expires_at = DATE_ADD(expires_at, INTERVAL 1 SECOND)
                """)).isInstanceOf(SQLException.class).hasMessageContaining("chk_dining_delivery_attempt_deadline");
            assertThatThrownBy(() -> statement.executeUpdate("""
                UPDATE dining_soldout_report_delivery_attempt
                SET mode = 'VERIFY', accepted_outcome = 'NOT_APPLIED', accepted_result = '{}',
                    result_at = '2026-10-02 12:45:30'
                """)).isInstanceOf(SQLException.class).hasMessageContaining("chk_dining_delivery_attempt_verify");
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
