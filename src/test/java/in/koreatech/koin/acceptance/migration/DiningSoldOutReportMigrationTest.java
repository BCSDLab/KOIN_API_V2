package in.koreatech.koin.acceptance.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import org.flywaydb.core.Flyway;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.model.DiningReport;
import in.koreatech.koin.domain.dining.model.DiningReportChange;
import in.koreatech.koin.domain.dining.model.DiningReportSequence;
import in.koreatech.koin.domain.dining.repository.DiningReportChangeRepository;

@Testcontainers
class DiningSoldOutReportMigrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.29")
        .withDatabaseName("dining_soldout_report_migration")
        .withUsername("test")
        .withPassword("test");

    @Test
    void V14와_V15는_기존이력을_보존하고_요청키없는_접수도_학생중복을_막는다() throws Exception {
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
            insertPendingReport(statement, 302, 2);
            statement.executeUpdate("UPDATE dining_soldout_report_sequence SET last_sequence = 2 WHERE id = 1");
        }
        migrateTo("13");
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            assertThat(queryInt(statement, "SELECT COUNT(*) FROM dining_soldout_report_delivery_target")).isZero();
            insertPendingReport(statement, 303, 3);
            insertLegacyTarget(statement, 303, 3, 0, "CONFLICT");
            insertLegacyDelivery(statement, 303, 3, "03", false);
            statement.executeUpdate("""
                INSERT INTO dining_soldout_report_delivery_attempt
                    (token, delivery_id, mode, send_attempt_token, issued_at, expires_at, evidence_history)
                VALUES (UNHEX(REPEAT('06', 16)), UNHEX(REPEAT('03', 16)), 'SEND', UNHEX(REPEAT('06', 16)),
                    '2026-10-02 12:45:00.123456', '2026-10-02 12:46:00.123456', '[]')
                """);
            statement.executeUpdate("""
                UPDATE dining_soldout_report
                SET status = 'APPROVED', processing_type = 'MANUAL', processing_id = UNHEX(REPEAT('07', 16)),
                    processed_at = '2026-10-02 12:50:00', updated_at = '2026-10-02 12:50:00',
                    processor_workspace_id = 'workspace', processor_user_id = 'processor', processor_name = '담당자'
                WHERE id = 303
                """);
            statement.executeUpdate("""
                INSERT INTO dining_soldout_report_change
                    (sequence, report_id, event_type, status, processing_type, processing_id, occurred_at)
                VALUES (4, 303, 'PROCESSED', 'APPROVED', 'MANUAL', UNHEX(REPEAT('07', 16)), '2026-10-02 12:50:00')
                """);
            statement.executeUpdate("""
                UPDATE dining_soldout_report_delivery_target
                SET desired_sequence = 4, desired_snapshot = '{"report_id":303,"status":"APPROVED"}'
                WHERE report_id = 303
                """);
            insertPendingReport(statement, 304, 5);
            insertLegacyTarget(statement, 304, 5, 0, "LEGACY");
            insertLegacyDelivery(statement, 304, 5, "04", false);
            insertPendingReport(statement, 305, 6);
            insertLegacyTarget(statement, 305, 6, 6, null);
            insertLegacyDelivery(statement, 305, 6, "05", true);
            statement.executeUpdate("UPDATE dining_soldout_report_sequence SET last_sequence = 6 WHERE id = 1");
        }
        migrateTo("14");
        try (SessionFactory sessionFactory = validateSchema();
            Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM dining_menus WHERE id = 201 AND menu = '기존 메뉴'
                    AND sold_out = '2026-10-02 12:30:00' AND sold_out_source IS NULL
                """)).isOne();
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM users u JOIN students s ON s.user_id = u.id
                WHERE u.id = 101 AND u.anonymous_nickname = '기존학생' AND s.student_number = '2025100101'
                """)).isOne();
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM dining_soldout_report_sequence
                WHERE id = 1 AND last_sequence = 6 AND delivery_cooldown_until IS NULL
                """)).isOne();
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM dining_soldout_report_change
                WHERE sequence IN (1, 2, 3, 6) AND delivery_state = 'DELIVERED'
                """)).isEqualTo(4);
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM dining_soldout_report_change
                WHERE sequence IN (1, 2) AND delivery_id IS NULL AND report_snapshot IS NULL
                    AND attempt_token IS NULL AND accepted_outcome IS NULL
                """)).isEqualTo(2);
            assertThat(queryInt(statement, "SELECT COUNT(*) FROM dining_soldout_report_change WHERE delivery_state = 'QUEUED'"))
                .isEqualTo(2);
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM dining_soldout_report_change WHERE sequence = 4 AND delivery_id IS NULL
                    AND delivery_state = 'QUEUED' AND JSON_UNQUOTE(JSON_EXTRACT(report_snapshot, '$.status')) = 'APPROVED'
                """)).isOne();
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM dining_soldout_report_change c
                JOIN dining_soldout_report_delivery d ON d.report_id = c.report_id AND d.source_sequence = c.sequence
                WHERE c.sequence IN (3, 5, 6) AND c.delivery_id = d.id AND c.report_snapshot = d.report_snapshot
                """)).isEqualTo(3);
            assertThat(queryInt(statement, "SELECT COUNT(*) FROM dining_soldout_report_delivery_target")).isEqualTo(3);
            assertThat(queryInt(statement, "SELECT COUNT(*) FROM dining_soldout_report_delivery")).isEqualTo(3);
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM dining_soldout_report_delivery_attempt
                WHERE token = UNHEX(REPEAT('06', 16)) AND mode = 'SEND'
                    AND MICROSECOND(issued_at) = 123456 AND evidence_history = '[]'
                """)).isOne();
            assertThatThrownBy(() -> statement.executeUpdate("""
                UPDATE dining_soldout_report_change SET delivery_id = UNHEX(REPEAT('04', 16)) WHERE sequence = 4
                """)).isInstanceOf(SQLException.class).hasMessageContaining("uk_dining_report_change_delivery");
            assertThatThrownBy(() -> statement.executeUpdate("""
                UPDATE dining_soldout_report_change SET report_snapshot = 'invalid-json' WHERE sequence = 4
                """)).isInstanceOf(SQLException.class).hasMessageContaining("chk_dining_report_change_snapshot");
            assertThatThrownBy(() -> statement.executeUpdate("""
                UPDATE dining_soldout_report_change SET delivery_state = 'UNCERTAIN' WHERE sequence = 4
                """)).isInstanceOf(SQLException.class).hasMessageContaining("chk_dining_report_change_delivery_state");
            assertThatThrownBy(() -> statement.executeUpdate("""
                UPDATE dining_soldout_report_change SET delivery_state = 'IN_PROGRESS' WHERE sequence = 4
                """)).isInstanceOf(SQLException.class).hasMessageContaining("chk_dining_report_change_lease");
            assertThatThrownBy(() -> statement.executeUpdate("""
                UPDATE dining_soldout_report_change SET accepted_outcome = 'NOT_APPLIED' WHERE sequence = 4
                """)).isInstanceOf(SQLException.class).hasMessageContaining("chk_dining_report_change_outcome");
            statement.executeUpdate("""
                UPDATE dining_soldout_report_change SET attempt_token = UNHEX(REPEAT('08', 16)) WHERE sequence = 5
                """);
            assertThatThrownBy(() -> statement.executeUpdate("""
                UPDATE dining_soldout_report_change SET attempt_token = UNHEX(REPEAT('08', 16)) WHERE sequence = 4
                """)).isInstanceOf(SQLException.class).hasMessageContaining("uk_dining_report_change_attempt");

            // A late old application uses only V12 columns and still produces claimable work after V14.
            insertPendingReport(statement, 306, 7);
            statement.executeUpdate("UPDATE dining_soldout_report_sequence SET last_sequence = 7 WHERE id = 1");
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM dining_soldout_report_change WHERE sequence = 7 AND delivery_state = 'QUEUED'
                    AND delivery_id IS NULL AND report_snapshot IS NULL AND attempt_token IS NULL
                    AND expires_at IS NULL AND next_attempt_at IS NULL AND accepted_outcome IS NULL
                """)).isOne();
            String claimQuery = DiningReportChangeRepository.class
                .getMethod("findClaimable", LocalDateTime.class, Pageable.class).getAnnotation(Query.class).value();
            try (Session session = sessionFactory.openSession()) {
                assertThat(session.createQuery(claimQuery, DiningReportChange.class)
                    .setParameter("now", LocalDateTime.of(2026, 10, 2, 13, 0)).getResultList())
                    .extracting(DiningReportChange::getSequence).containsExactly(4L, 5L, 7L);
            }
            statement.executeUpdate("DELETE FROM users WHERE id = 101");
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM dining_soldout_report WHERE id = 301 AND reporter_id IS NULL
                    AND status = 'APPROVED' AND processor_name = '담당자' AND processing_type = 'MANUAL'
                    AND processing_id = UNHEX(REPEAT('02', 16))
                """)).isOne();
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM dining_soldout_report_change WHERE sequence = 1 AND report_id = 301
                    AND event_type = 'PROCESSED' AND status = 'APPROVED' AND processing_type = 'MANUAL'
                    AND processing_id = UNHEX(REPEAT('02', 16)) AND delivery_state = 'DELIVERED'
                """)).isOne();
        }
        migrateTo("15");
        try (SessionFactory sessionFactory = validateSchema();
            Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM dining_soldout_report
                WHERE id BETWEEN 301 AND 306 AND request_key = UNHEX(REPEAT('01', 16))
                """)).isEqualTo(6);
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM dining_soldout_report WHERE id = 301 AND reporter_id IS NULL
                    AND status = 'APPROVED' AND processor_name = '담당자' AND processing_type = 'MANUAL'
                    AND processing_id = UNHEX(REPEAT('02', 16))
                """)).isOne();
            statement.executeUpdate("""
                INSERT INTO users (id, password, user_type, anonymous_nickname)
                VALUES (102, 'test', 'STUDENT', '새학생1'), (103, 'test', 'STUDENT', '새학생2')
                """);
            statement.executeUpdate("""
                INSERT INTO dining_menus (id, date, type, place, menu)
                VALUES (202, '2026-10-02', 'LUNCH', 'B코너', '새 메뉴')
                """);
            statement.executeUpdate("""
                INSERT INTO dining_soldout_report (id, reporter_id, dining_id, image_url, created_at, updated_at)
                VALUES (307, 102, 201, 'https://example.com/new.jpg', '2026-10-02 12:45:00', '2026-10-02 12:45:00'),
                       (308, 103, 201, 'https://example.com/new.jpg', '2026-10-02 12:45:00', '2026-10-02 12:45:00'),
                       (309, 102, 202, 'https://example.com/new.jpg', '2026-10-02 12:45:00', '2026-10-02 12:45:00')
                """);
            assertThatThrownBy(() -> statement.executeUpdate("""
                INSERT INTO dining_soldout_report (reporter_id, dining_id, image_url, created_at, updated_at)
                VALUES (102, 201, 'https://example.com/new.jpg', '2026-10-02 12:45:00', '2026-10-02 12:45:00')
                """)).isInstanceOf(SQLException.class).hasMessageContaining("uk_dining_report_student");
            assertThat(queryInt(statement, """
                SELECT COUNT(*) FROM dining_soldout_report WHERE reporter_id IN (102, 103) AND request_key IS NULL
                """)).isEqualTo(3);
        }
    }

    private static void insertPendingReport(Statement statement, int reportId, long sequence) throws SQLException {
        statement.executeUpdate("""
            INSERT INTO dining_soldout_report (id, dining_id, image_url, request_key, created_at, updated_at)
            VALUES (%d, 201, 'https://example.com/new.jpg', UNHEX(REPEAT('01', 16)),
                '2026-10-02 12:45:00', '2026-10-02 12:45:00')
            """.formatted(reportId));
        statement.executeUpdate("""
            INSERT INTO dining_soldout_report_change (sequence, report_id, event_type, status, occurred_at)
            VALUES (%d, %d, 'CREATED', 'PENDING', '2026-10-02 12:45:00')
            """.formatted(sequence, reportId));
    }

    private static void insertLegacyTarget(Statement statement, int reportId, long sequence, long confirmed,
        String hold) throws SQLException {
        statement.executeUpdate("""
            INSERT INTO dining_soldout_report_delivery_target
                (report_id, desired_sequence, desired_snapshot, confirmed_sequence, hold_reason, created_at, updated_at)
            VALUES (%d, %d, '{"report_id":%d,"status":"PENDING"}', %d, %s,
                '2026-10-02 12:45:00.123456', '2026-10-02 12:45:00.123456')
            """.formatted(reportId, sequence, reportId, confirmed, hold == null ? "NULL" : "'" + hold + "'"));
    }

    private static void insertLegacyDelivery(Statement statement, int reportId, long sequence, String id,
        boolean delivered) throws SQLException {
        statement.executeUpdate("""
            INSERT INTO dining_soldout_report_delivery
                (id, report_id, source_sequence, report_snapshot, operation, workspace_id, channel_id,
                 status, confirmed_message_ts, created_at, updated_at)
            VALUES (UNHEX(REPEAT('%s', 16)), %d, %d, '{"report_id":%d,"status":"PENDING"}',
                'CREATE', 'T_EXAMPLE', 'C_EXAMPLE', '%s', %s,
                '2026-10-02 12:45:00.123456', '2026-10-02 12:45:00.123456')
            """.formatted(id, reportId, sequence, reportId, delivered ? "DELIVERED" : "QUEUED",
                delivered ? "'123.000'" : "NULL"));
    }

    private static SessionFactory validateSchema() {
        return new Configuration()
            .addAnnotatedClass(Dining.class)
            .addAnnotatedClass(DiningReport.class)
            .addAnnotatedClass(DiningReportChange.class)
            .addAnnotatedClass(DiningReportSequence.class)
            .setProperty("hibernate.connection.url", MYSQL.getJdbcUrl())
            .setProperty("hibernate.connection.username", MYSQL.getUsername())
            .setProperty("hibernate.connection.password", MYSQL.getPassword())
            .setProperty("hibernate.hbm2ddl.auto", "validate")
            .buildSessionFactory();
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
