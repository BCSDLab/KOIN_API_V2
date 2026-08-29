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
