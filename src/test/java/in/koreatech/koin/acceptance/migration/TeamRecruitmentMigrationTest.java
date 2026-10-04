package in.koreatech.koin.acceptance.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class TeamRecruitmentMigrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.29")
        .withDatabaseName("team_recruitment_migration")
        .withUsername("test")
        .withPassword("test");

    @BeforeAll
    static void migrate() {
        Flyway.configure()
            .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()
            .migrate();
    }

    @Test
    void 팀원_모집_공통_테이블과_핵심_제약을_생성한다() throws SQLException {
        try (Connection connection = DriverManager.getConnection(
            MYSQL.getJdbcUrl(),
            MYSQL.getUsername(),
            MYSQL.getPassword()
        )) {
            assertThat(queryInt(connection, tableCountQuery("team_recruitment"))).isOne();
            assertThat(queryInt(connection, tableCountQuery("team_recruitment_role"))).isOne();
            assertThat(queryInt(connection, tableCountQuery("team_recruitment_profile"))).isOne();
            assertThat(queryInt(connection, tableCountQuery("team_recruitment_application"))).isOne();
            assertThat(queryInt(connection, tableCountQuery("team_recruitment_chat_room"))).isOne();
            assertThat(queryInt(connection, tableCountQuery("team_recruitment_chat_member"))).isOne();
            assertThat(queryInt(connection, tableCountQuery("team_recruitment_chat_message"))).isOne();
            assertThat(queryInt(connection, tableCountQuery("team_recruitment_notification"))).isOne();
            assertThat(queryInt(connection, tableCountQuery("team_recruitment_outbox_event"))).isOne();

            assertThat(queryString(connection, columnTypeQuery("team_recruitment_chat_message", "content")))
                .isEqualTo("text");
            assertThat(queryString(connection, nullableColumnQuery("team_recruitment_chat_message", "content")))
                .isEqualTo("NO");
            assertThat(queryString(connection, nullableColumnQuery(
                "team_recruitment_chat_message",
                "sender_nickname"
            ))).isEqualTo("NO");
            assertThat(queryString(connection, nullableColumnQuery("team_recruitment_chat_message", "is_image")))
                .isEqualTo("NO");
            assertThat(queryString(connection, nullableColumnQuery(
                "team_recruitment_chat_member",
                "last_read_message_id"
            ))).isEqualTo("YES");
            assertThat(queryString(connection, foreignKeyColumnsQuery(
                "team_recruitment_chat_member",
                "fk_team_recruitment_chat_member_last_read_message"
            ))).isEqualTo("last_read_message_id,chat_room_id");
            assertThat(queryString(connection, indexColumnsQuery(
                "team_recruitment_chat_message",
                "idx_team_recruitment_chat_message_room_id_id"
            ))).isEqualTo("chat_room_id,id");
            assertThat(queryInt(connection, foreignKeyQuery(
                "team_recruitment_chat_message",
                "sender_id",
                "users"
            ))).isOne();
            assertThat(queryString(connection, checkConstraintQuery(
                "team_recruitment_chat_message",
                "chk_team_recruitment_chat_message_content"
            )).toLowerCase(Locale.ROOT))
                .contains("char_length")
                .contains("trim");
            assertThat(queryString(connection, checkConstraintQuery(
                "team_recruitment",
                "chk_team_recruitment_deleted_at"
            )).toLowerCase(Locale.ROOT))
                .contains("deleted")
                .contains("deleted_at");
            assertThat(queryString(connection, checkConstraintQuery(
                "team_recruitment",
                "chk_team_recruitment_dates"
            )).toLowerCase(Locale.ROOT))
                .contains("activity_start_date")
                .contains("activity_end_date")
                .doesNotContain("deadline_date");
            assertThat(queryString(connection, checkConstraintQuery(
                "team_recruitment_chat_room",
                "chk_team_recruitment_chat_room_application_scope"
            )).toLowerCase(Locale.ROOT))
                .contains("room_scope_key")
                .contains("application_id");

            assertThat(queryString(connection, columnTypeQuery("team_recruitment_application", "profile_snapshot")))
                .isEqualTo("json");
            assertThat(queryInt(connection, uniqueIndexQuery(
                "team_recruitment_application",
                "uk_team_recruitment_application_recruitment_applicant"
            ))).isEqualTo(2);
            assertThat(queryString(connection, nullableColumnQuery(
                "team_recruitment_notification",
                "sender_nickname"
            ))).isEqualTo("YES");
            assertThat(queryString(connection, nullableColumnQuery(
                "team_recruitment_notification",
                "is_deleted"
            ))).isEqualTo("NO");
            assertThat(queryString(connection, indexColumnsQuery(
                "team_recruitment_application",
                "uk_team_recruitment_application_id_recruitment"
            ))).isEqualTo("id,recruitment_id");
            assertThat(queryString(connection, indexColumnsQuery(
                "team_recruitment_chat_room",
                "uk_team_recruitment_chat_room_recruitment_application_type"
            ))).isEqualTo("recruitment_id,application_id,room_type");
            assertThat(queryInt(connection, uniqueIndexQuery(
                "team_recruitment_chat_member",
                "uk_team_recruitment_chat_member_room_user"
            ))).isEqualTo(2);
            assertThat(queryString(connection, nullableColumnQuery(
                "team_recruitment_outbox_event",
                "locked_until"
            ))).isEqualTo("YES");
            assertThat(queryString(connection, nullableColumnQuery(
                "team_recruitment_outbox_event",
                "worker_id"
            ))).isEqualTo("YES");
            assertThat(queryString(connection, indexColumnsQuery(
                "team_recruitment_outbox_event",
                "idx_team_recruitment_outbox_claim"
            ))).isEqualTo("status,next_attempt_at,locked_until,attempt_count,id");
        }
    }

    @Test
    void 신고된_모집글과_알림의_오타만_교정하고_재실행해도_값이_유지된다() throws SQLException {
        try (Connection connection = typoFixtureConnection()) {
            try {
                ScriptUtils.executeSqlScript(connection, new ClassPathResource(
                    "db/migration/V12__correct_team_recruitment_notification_typo.sql"));

                assertThat(queryString(connection, "SELECT title FROM team_recruitment WHERE id = 71"))
                    .isEqualTo("공모전");
                assertThat(queryString(connection,
                    "SELECT message_preview FROM team_recruitment_notification WHERE id = 136"))
                    .isEqualTo("지원하신 공모전 모집이 마감되어 지원이 거절되었어요.");
                assertThat(queryInt(connection, """
                    SELECT COUNT(*) FROM team_recruitment_notification
                    WHERE id = 136 AND recruitment_id = 71 AND application_id = 52
                        AND type = 'APPLICATION_REJECTED' AND target_type = 'MY_APPLICATIONS'
                        AND read_at IS NULL AND is_deleted = 0
                        AND created_at = '2026-09-29 00:00:32'
                    """)).isOne();
                assertThat(queryString(connection, "SELECT title FROM team_recruitment WHERE id = 72"))
                    .isEqualTo("공모저온");
                assertThat(queryInt(connection, """
                    SELECT COUNT(*) FROM team_recruitment_notification
                    WHERE id IN (137, 138)
                        AND message_preview = '지원하신 공모저온 모집이 마감되어 지원이 거절되었어요.'
                    """)).isEqualTo(2);

                execute(connection, "UPDATE team_recruitment SET title = '작성자가 수정한 제목' WHERE id = 71");
                ScriptUtils.executeSqlScript(connection, new ClassPathResource(
                    "db/migration/V12__correct_team_recruitment_notification_typo.sql"));

                assertThat(queryString(connection, "SELECT title FROM team_recruitment WHERE id = 71"))
                    .isEqualTo("작성자가 수정한 제목");
                assertThat(queryString(connection,
                    "SELECT message_preview FROM team_recruitment_notification WHERE id = 136"))
                    .isEqualTo("지원하신 공모전 모집이 마감되어 지원이 거절되었어요.");
            } finally {
                connection.rollback();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "id = 139",
        "recruitment_id = 72, application_id = 53",
        "application_id = 51",
        "type = 'RECRUITMENT_CLOSED'",
        "target_type = 'NONE'",
        "message_preview = '작성자가 수정한 문구'",
        "message_preview = '지원하신 공모저온 모집이 마감되어 지원이 거절되었어요. '"
    })
    void 대상_알림의_ID나_기존_값이_다르면_문구를_덮어쓰지_않는다(String changedValues) throws SQLException {
        try (Connection connection = typoFixtureConnection()) {
            try {
                execute(connection, "UPDATE team_recruitment_notification SET " + changedValues + " WHERE id = 136");
                String previousMessage = queryString(connection,
                    "SELECT message_preview FROM team_recruitment_notification WHERE id NOT IN (137, 138)");

                ScriptUtils.executeSqlScript(connection, new ClassPathResource(
                    "db/migration/V12__correct_team_recruitment_notification_typo.sql"));

                assertThat(queryString(connection,
                    "SELECT message_preview FROM team_recruitment_notification WHERE id NOT IN (137, 138)"))
                    .isEqualTo(previousMessage);
            } finally {
                connection.rollback();
            }
        }
    }

    private Connection typoFixtureConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(
            MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
        connection.setAutoCommit(false);
        try {
            execute(connection, """
                INSERT INTO users (id, password, user_type, anonymous_nickname)
                VALUES (101, 'test', 'GENERAL', '오타교정작성자'),
                       (102, 'test', 'GENERAL', '오타교정지원자'),
                       (103, 'test', 'GENERAL', '오타교정다른지원자')
                """, """
                INSERT INTO team_recruitment
                    (id, author_id, category, title, meeting_type, activity_start_date, activity_end_date,
                     deadline_date, recruitment_type, max_participants, description, status)
                VALUES (71, 101, 'CONTEST', '공모저온', 'ONLINE', '2026-09-29', '2026-10-05',
                        '2026-09-28', 'GENERAL', 5, '테스트 모집글', 'CLOSED'),
                       (72, 101, 'CONTEST', '공모저온', 'ONLINE', '2026-09-29', '2026-10-05',
                        '2026-09-28', 'GENERAL', 5, '다른 모집글', 'CLOSED')
                """, """
                INSERT INTO team_recruitment_application
                    (id, recruitment_id, applicant_id, motivation, availability, status, profile_snapshot)
                VALUES (51, 71, 103, '테스트 지원', '가능', 'REJECTED', '{}'),
                       (52, 71, 102, '테스트 지원', '가능', 'REJECTED', '{}'),
                       (53, 72, 102, '다른 지원', '가능', 'REJECTED', '{}')
                """, """
                INSERT INTO team_recruitment_notification
                    (id, recipient_id, type, target_type, message_preview, recruitment_id, application_id, created_at)
                VALUES (136, 102, 'APPLICATION_REJECTED', 'MY_APPLICATIONS',
                        '지원하신 공모저온 모집이 마감되어 지원이 거절되었어요.', 71, 52, '2026-09-29 00:00:32'),
                       (137, 102, 'APPLICATION_REJECTED', 'MY_APPLICATIONS',
                        '지원하신 공모저온 모집이 마감되어 지원이 거절되었어요.', 71, 52, '2026-09-29 00:00:32'),
                       (138, 102, 'APPLICATION_REJECTED', 'MY_APPLICATIONS',
                        '지원하신 공모저온 모집이 마감되어 지원이 거절되었어요.', 72, 53, '2026-09-29 00:00:32')
                """);
            return connection;
        } catch (SQLException exception) {
            connection.close();
            throw exception;
        }
    }

    private void execute(Connection connection, String... queries) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String query : queries) {
                statement.executeUpdate(query);
            }
        }
    }

    private String tableCountQuery(String tableName) {
        return """
            SELECT COUNT(*)
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '%s'
            """.formatted(tableName);
    }

    private String columnTypeQuery(String tableName, String columnName) {
        return """
            SELECT DATA_TYPE
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
                AND TABLE_NAME = '%s'
                AND COLUMN_NAME = '%s'
        """.formatted(tableName, columnName);
    }

    private String nullableColumnQuery(String tableName, String columnName) {
        return """
            SELECT IS_NULLABLE
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
                AND TABLE_NAME = '%s'
                AND COLUMN_NAME = '%s'
            """.formatted(tableName, columnName);
    }

    private String indexColumnsQuery(String tableName, String indexName) {
        return """
            SELECT GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX)
            FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
                AND TABLE_NAME = '%s'
                AND INDEX_NAME = '%s'
            """.formatted(tableName, indexName);
    }

    private String foreignKeyQuery(String tableName, String columnName, String referencedTable) {
        return """
            SELECT COUNT(*)
            FROM information_schema.KEY_COLUMN_USAGE
            WHERE TABLE_SCHEMA = DATABASE()
                AND TABLE_NAME = '%s'
                AND COLUMN_NAME = '%s'
                AND REFERENCED_TABLE_NAME = '%s'
        """.formatted(tableName, columnName, referencedTable);
    }

    private String foreignKeyColumnsQuery(String tableName, String constraintName) {
        return """
            SELECT GROUP_CONCAT(COLUMN_NAME ORDER BY ORDINAL_POSITION)
            FROM information_schema.KEY_COLUMN_USAGE
            WHERE TABLE_SCHEMA = DATABASE()
                AND TABLE_NAME = '%s'
                AND CONSTRAINT_NAME = '%s'
            """.formatted(tableName, constraintName);
    }

    private String checkConstraintQuery(String tableName, String constraintName) {
        return """
            SELECT checks.CHECK_CLAUSE
            FROM information_schema.CHECK_CONSTRAINTS AS checks
            JOIN information_schema.TABLE_CONSTRAINTS AS tables
                ON tables.CONSTRAINT_SCHEMA = checks.CONSTRAINT_SCHEMA
                AND tables.CONSTRAINT_NAME = checks.CONSTRAINT_NAME
            WHERE checks.CONSTRAINT_SCHEMA = DATABASE()
                AND tables.TABLE_NAME = '%s'
                AND checks.CONSTRAINT_NAME = '%s'
            """.formatted(tableName, constraintName);
    }

    private String uniqueIndexQuery(String tableName, String indexName) {
        return """
            SELECT COUNT(*)
            FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
                AND TABLE_NAME = '%s'
                AND INDEX_NAME = '%s'
                AND NON_UNIQUE = 0
            """.formatted(tableName, indexName);
    }

    private int queryInt(Connection connection, String query) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(query)) {
            result.next();
            return result.getInt(1);
        }
    }

    private String queryString(Connection connection, String query) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(query)) {
            result.next();
            return result.getString(1);
        }
    }
}
