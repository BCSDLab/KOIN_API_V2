package in.koreatech.koin.acceptance.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class CallvanReadCursorMigrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.29")
        .withDatabaseName("callvan_read_migration").withUsername("test").withPassword("test");

    @Test
    void 기존_대화는_보존하고_방별_마지막_메시지로_읽음을_초기화한다() throws Exception {
        migrate("30");
        try (Connection connection = connect(); Statement sql = connection.createStatement()) {
            sql.executeUpdate("""
                INSERT INTO callvan_chat_room (id, callvan_post_id, room_name)
                VALUES (1, 10, '기존 방'), (2, 20, '빈 방'), (3, 30, '다른 방')
                """);
            sql.executeUpdate("""
                INSERT INTO callvan_participant (id, post_id, member_id, is_deleted)
                VALUES (1, 10, 100, 0), (2, 10, 101, 1), (3, 20, 100, 0), (4, 30, 100, 0)
                """);
            sql.executeUpdate("""
                INSERT INTO callvan_chat_message
                    (id, callvan_chat_room_id, sender_id, sender_nickname, content, is_deleted)
                VALUES (10, 1, 100, '기존 이름', '첫 메시지', 0),
                    (11, 1, 101, '나간 참여자', '다음 메시지', 0),
                    (12, 1, 100, '기존 이름', '삭제된 메시지', 1),
                    (20, 3, 100, '다른 방 이름', '다른 방 메시지', 0)
                """);
            List<String> before = messages(connection);

            migrate("31");

            List<Integer> cursors = new ArrayList<>();
            try (ResultSet rows = sql.executeQuery(
                "SELECT last_read_message_id FROM callvan_participant ORDER BY id")) {
                while (rows.next()) {
                    cursors.add(rows.getInt(1));
                }
            }
            assertThat(cursors).containsExactly(11, 11, 0, 20);
            assertThat(messages(connection)).isEqualTo(before);
            sql.executeUpdate("INSERT INTO callvan_participant (post_id, member_id) VALUES (20, 102)");
            try (ResultSet row = sql.executeQuery(
                "SELECT last_read_message_id FROM callvan_participant WHERE member_id = 102")) {
                assertThat(row.next()).isTrue();
                assertThat(row.getInt(1)).isZero();
                assertThat(row.wasNull()).isFalse();
            }
            assertThat(migrate("31")).isZero();
        }
    }

    private int migrate(String target) {
        return Flyway.configure().dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
            .locations("classpath:db/migration").target(target).load().migrate().migrationsExecuted;
    }

    private Connection connect() throws Exception {
        return DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
    }

    private List<String> messages(Connection connection) throws Exception {
        List<String> messages = new ArrayList<>();
        try (Statement sql = connection.createStatement(); ResultSet rows = sql.executeQuery(
            "SELECT id, content, sender_nickname, is_deleted FROM callvan_chat_message ORDER BY id")) {
            while (rows.next()) {
                messages.add(rows.getInt(1) + ":" + rows.getString(2) + ":" + rows.getString(3) + ":" + rows.getInt(4));
            }
        }
        return messages;
    }
}
