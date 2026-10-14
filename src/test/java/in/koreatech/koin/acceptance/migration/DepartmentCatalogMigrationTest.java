package in.koreatech.koin.acceptance.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class DepartmentCatalogMigrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.29")
        .withDatabaseName("department_catalog_migration")
        .withUsername("test")
        .withPassword("test");

    private static List<List<String>> departmentsBefore;
    private static List<List<String>> studentsBefore;

    @BeforeAll
    static void migrate() throws Exception {
        migrateTo("17");
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                INSERT INTO department (id, name, created_at, updated_at) VALUES
                    (11, '컴퓨터공학부', '2020-01-01', '2020-01-02'),
                    (12, '응용화학공학부', '2020-01-01', '2020-01-02'),
                    (13, '학과공통', '2020-01-01', '2020-01-02'),
                    (14, '에너지신소재화학공학부', '2020-01-01', '2020-01-02'),
                    (15, '건축공학부', '2020-01-01', '2020-01-02')
                """);
            statement.executeUpdate("INSERT INTO major (id, department_id, name) VALUES (1, 11, NULL)");
            statement.executeUpdate("""
                INSERT INTO students (user_id, student_number, department_id, major_id)
                VALUES (1, '2026135001', 11, 1)
                """);
            departmentsBefore = rows(connection,
                "SELECT id, name, created_at, updated_at FROM department ORDER BY id");
            studentsBefore = rows(connection, "SELECT * FROM students ORDER BY user_id");
        }
        migrateTo("18");
    }

    @Test
    void 기존_학부_ID와_이름_타임스탬프_학적_연결을_보존한다() throws Exception {
        try (Connection connection = connect()) {
            assertThat(rows(connection, "SELECT id, name, created_at, updated_at FROM department ORDER BY id"))
                .isEqualTo(departmentsBefore);
            assertThat(rows(connection, "SELECT * FROM students ORDER BY user_id"))
                .isEqualTo(studentsBefore);
            assertThat(rows(connection, "SELECT department_id FROM major WHERE id = 1"))
                .containsExactly(List.of("11"));
        }
    }

    @Test
    void 기존_응답의_메타데이터와_표시순서를_이관하고_DB_전용_학부도_노출한다() throws Exception {
        try (Connection connection = connect()) {
            assertThat(rows(connection, """
                SELECT name, curriculum_link, dept_nums, display_order
                FROM department WHERE is_selectable = 1 ORDER BY display_order, id
                """))
                .containsExactly(
                    List.of("건축공학부", "https://www.koreatech.ac.kr/menu.es?mid=b30202000000", "[\"72\"]", "1"),
                    List.of("컴퓨터공학부", "https://www.koreatech.ac.kr/menu.es?mid=b10402000000", "[\"35\", \"36\"]", "8"),
                    List.of("응용화학공학부", "https://www.koreatech.ac.kr/menu.es?mid=a10201000000", "[\"N/A\"]", "1000"),
                    List.of("에너지신소재화학공학부", "https://cms3.koreatech.ac.kr/ace/992/subview.do", "[\"74\"]", "1000")
                );
            assertThat(rows(connection, "SELECT name FROM department WHERE is_selectable = 0 ORDER BY id"))
                .containsExactly(List.of("학과공통"));
        }
    }

    @Test
    void 이후_추가하는_학부도_별도_코드_수정없이_유효한_기본값으로_조회할_수_있다() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            try {
                statement.executeUpdate("INSERT INTO department (name) VALUES ('테스트신설학부')");
                assertThat(rows(connection, """
                    SELECT curriculum_link, dept_nums, is_selectable, display_order
                    FROM department WHERE name = '테스트신설학부'
                    """))
                    .containsExactly(List.of(
                        "https://www.koreatech.ac.kr/menu.es?mid=a10201000000", "[\"N/A\"]", "1", "1000"));
            } finally {
                connection.rollback();
            }
        }
    }

    private static void migrateTo(String version) {
        Flyway.configure().dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
            .locations("classpath:db/migration").target(version).load().migrate();
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
    }

    private static List<List<String>> rows(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            List<List<String>> rows = new ArrayList<>();
            while (result.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
                    row.add(result.getString(i));
                }
                rows.add(row);
            }
            return rows;
        }
    }
}
