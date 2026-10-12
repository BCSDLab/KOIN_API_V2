package in.koreatech.koin.acceptance.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class AcademicProgramYearMigrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.29")
        .withDatabaseName("academic_program_migration")
        .withUsername("test")
        .withPassword("test");

    private static List<List<String>> legacyBeforeMigration;

    @BeforeAll
    static void migrate() throws Exception {
        Flyway.configure().dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
            .locations("classpath:db/migration").target("16").load().migrate();
        // 운영자가 먼저 준비한 테이블과 배포 시 Flyway V17이 함께 적용되어도 충돌하지 않는다.
        try (Connection connection = connect()) {
            seedLegacyData(connection);
            legacyBeforeMigration = snapshot(connection);
            ScriptUtils.executeSqlScript(connection,
                new ClassPathResource("db/migration/V17__create_academic_program_year.sql"));
        }
        Flyway.configure().dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
            .locations("classpath:db/migration").target("17").load().migrate();
    }

    @BeforeEach
    void clearYearMappings() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM academic_program_year");
        }
    }

    private static void seedLegacyData(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO department (id, name) VALUES (1, '테스트기존학부')");
            statement.executeUpdate("INSERT INTO major (id, department_id, name) VALUES (1, 1, NULL)");
            statement.executeUpdate("""
                INSERT INTO students (user_id, student_number, department_id, major_id) VALUES
                (1, '2019136001', 1, 1), (2, '2026136001', 1, NULL)
                """);
            statement.executeUpdate("""
                INSERT INTO standard_graduation_requirements
                    (year, major_id, required_grades) VALUES ('2024', 1, 42)
                """);
        }
    }

    @Test
    void V17_적용과_재실행이_기존_학적과_새_연도_연결을_보존한다() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertThat(snapshot(connection)).isEqualTo(legacyBeforeMigration);
            statement.executeUpdate("""
                INSERT INTO academic_program_year (student_year, department_id, major_id)
                VALUES ('2026', 1, 1)
                """);
            List<List<String>> programs = rows(connection, "SELECT * FROM academic_program_year ORDER BY id");
            ScriptUtils.executeSqlScript(connection,
                new ClassPathResource("db/migration/V17__create_academic_program_year.sql"));
            assertThat(rows(connection, "SELECT * FROM academic_program_year ORDER BY id")).isEqualTo(programs);
        }
        try (Connection connection = connect()) {
            assertThat(snapshot(connection)).isEqualTo(legacyBeforeMigration);
            assertThat(rows(connection, """
                SELECT student_year, department_id, major_id FROM academic_program_year
                """)).containsExactly(List.of("2026", "1", "1"));
        }
    }

    @Test
    void 커밋_전_취소는_추가_행을_되돌리고_기존_데이터를_보존한다() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            statement.executeUpdate("""
                INSERT INTO academic_program_year (student_year, department_id, major_id)
                VALUES ('2027', 1, NULL)
                """);
            assertThat(rows(connection, "SELECT COUNT(*) FROM academic_program_year")).containsExactly(List.of("1"));
            connection.rollback();
            assertThat(snapshot(connection)).isEqualTo(legacyBeforeMigration);
            assertThat(rows(connection, "SELECT COUNT(*) FROM academic_program_year")).containsExactly(List.of("0"));
        }
    }

    @Test
    void NULL_전공도_중복_연결을_허용하지_않는다() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                INSERT INTO academic_program_year (student_year, department_id, major_id)
                VALUES ('2026', 1, NULL)
                """);
            assertThatThrownBy(() -> statement.executeUpdate("""
                INSERT INTO academic_program_year (student_year, department_id, major_id)
                VALUES ('2026', 1, NULL)
                """))
                .isInstanceOf(SQLException.class).hasMessageContaining("Duplicate entry");
        }
    }

    @Test
    void V17_적용_후_존재하지_않는_학부나_전공의_연결을_거절한다() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertThat(rows(connection, "SELECT version FROM flyway_schema_history WHERE success = 1 ORDER BY installed_rank"))
                .hasSize(17).last().isEqualTo(List.of("17"));
            assertThatThrownBy(() -> statement.executeUpdate("""
                INSERT INTO academic_program_year (student_year, department_id, major_id)
                VALUES ('2027', 999999, NULL)
                """))
                .isInstanceOf(SQLException.class).hasMessageContaining("foreign key constraint fails");
            assertThatThrownBy(() -> statement.executeUpdate("""
                INSERT INTO academic_program_year (student_year, department_id, major_id)
                VALUES ('2027', 1, 999999)
                """))
                .isInstanceOf(SQLException.class).hasMessageContaining("foreign key constraint fails");
        }
    }

    private static List<List<String>> snapshot(Connection connection) throws SQLException {
        List<List<String>> result = new ArrayList<>();
        result.addAll(rows(connection, "SELECT * FROM department ORDER BY id"));
        result.addAll(rows(connection, "SELECT * FROM major ORDER BY id"));
        result.addAll(rows(connection, "SELECT * FROM students ORDER BY user_id"));
        result.addAll(rows(connection, "SELECT * FROM standard_graduation_requirements ORDER BY id"));
        return result;
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
