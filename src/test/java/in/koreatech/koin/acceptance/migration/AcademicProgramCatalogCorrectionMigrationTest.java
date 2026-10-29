package in.koreatech.koin.acceptance.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class AcademicProgramCatalogCorrectionMigrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.29")
        .withDatabaseName("academic_catalog_correction")
        .withUsername("test")
        .withPassword("test");

    private static final String MIGRATION = "db/migration/V20__correct_academic_program_catalog_2025_2026.sql";

    // Reviewed handbook catalogue: ~ denotes a NULL name; the last field distinguishes a
    // department-only mapping from an actual nameless major. This oracle does not read V20.
    private static final String EXPECTED = """
        2025|기계공학부|~|0
        2025|메카트로닉스공학부|~|0
        2025|전기전자통신공학부|~|0
        2025|컴퓨터공학부|~|0
        2025|디자인·건축공학부|~|0
        2025|에너지신소재화학공학부|~|0
        2025|산업경영학부|~|0
        2025|고용서비스정책학과|~|0
        2026|기계공학부|~|0
        2026|메카트로닉스공학부|~|0
        2026|디자인공학과|~|0
        2026|건축공학과|~|0
        2026|에너지신소재화학공학부|~|0
        2026|공학융합자율전공|~|0
        2026|전기전자통신공학부|~|0
        2026|컴퓨터공학부|~|0
        2026|ICT융합자율전공|~|0
        2026|경영학부|~|0
        2026|고용서비스정책학과|~|0
        2026|사회융합자율전공|~|0
        2026|미래융합학부|~|0
        2026|글로벌인재학부|~|0
        2026|반도체·디스플레이공학과|~|0
        2025|메카트로닉스공학부|생산시스템전공|1
        2025|메카트로닉스공학부|제어시스템전공|1
        2025|메카트로닉스공학부|디지털시스템전공|1
        2025|전기전자통신공학부|전기공학전공|1
        2025|전기전자통신공학부|전자공학전공|1
        2025|전기전자통신공학부|정보통신공학전공|1
        2025|디자인·건축공학부|디자인공학전공|1
        2025|디자인·건축공학부|건축공학전공|1
        2025|에너지신소재화학공학부|에너지신소재공학전공|1
        2025|에너지신소재화학공학부|화학생명공학전공|1
        2025|산업경영학부|융합경영전공|1
        2025|산업경영학부|데이터경영전공|1
        2026|메카트로닉스공학부|생산시스템전공|1
        2026|메카트로닉스공학부|제어시스템전공|1
        2026|메카트로닉스공학부|디지털시스템전공|1
        2026|에너지신소재화학공학부|에너지신소재공학전공|1
        2026|에너지신소재화학공학부|화학생명공학전공|1
        2026|전기전자통신공학부|전기공학전공|1
        2026|전기전자통신공학부|전자공학전공|1
        2026|전기전자통신공학부|AI정보통신공학전공|1
        2026|컴퓨터공학부|컴퓨터공학전공|1
        2026|컴퓨터공학부|AI·소프트웨어전공|1
        2026|경영학부|융합경영전공|1
        2026|경영학부|데이터경영전공|1
        2026|글로벌인재학부|기계공학전공|1
        2026|글로벌인재학부|컴퓨터공학전공|1
        2025|컴퓨터공학부|~|1
        2025|기계공학부|~|1
        2025|고용서비스정책학과|~|1
        2026|기계공학부|~|1
        2026|고용서비스정책학과|~|1
        2026|디자인공학과|~|1
        2026|건축공학과|~|1
        2025|기전융합공학과|~|0
        2025|기계설계공학과|~|0
        2025|강소기업경영학과|~|0
        2026|기전융합공학과|~|0
        2026|기계설계공학과|~|0
        2026|강소기업경영학과|~|0
        """;

    // Historical seed before the manual correction (40 mappings for 2025, 35 for 2026).
    private static final String FAULTY = """
        2025|컴퓨터공학부|~|0
        2025|기계공학부|~|0
        2025|메카트로닉스공학부|~|0
        2025|전기전자통신공학부|~|0
        2025|디자인공학부|~|0
        2025|건축공학부|~|0
        2025|화학생명공학부|~|0
        2025|에너지신소재공학부|~|0
        2025|산업경영학부|~|0
        2025|고용서비스정책학과|~|0
        2025|응용화학공학부|~|0
        2025|공학융합자율전공|~|0
        2025|ICT융합자율전공|~|0
        2025|사회융합자율전공|~|0
        2025|반도체·디스플레이공학과|~|0
        2025|미래융합학부|~|0
        2025|에너지신소재화학공학부|~|0
        2025|디자인·건축공학부|~|0
        2025|컴퓨터공학부|~|1
        2025|기계공학부|~|1
        2025|메카트로닉스공학부|생산시스템전공|1
        2025|메카트로닉스공학부|제어시스템전공|1
        2025|메카트로닉스공학부|디지털시스템전공|1
        2025|전기전자통신공학부|전기공학전공|1
        2025|전기전자통신공학부|전자공학전공|1
        2025|전기전자통신공학부|정보통신공학전공|1
        2025|디자인공학부|디자인공학전공|1
        2025|건축공학부|건축공학전공|1
        2025|화학생명공학부|화학생명공학전공|1
        2025|에너지신소재공학부|에너지신소재공학전공|1
        2025|산업경영학부|데이터경영전공|1
        2025|산업경영학부|산업경영전공|1
        2025|산업경영학부|혁신경영전공|1
        2025|산업경영학부|융합경영전공|1
        2025|고용서비스정책학과|~|1
        2025|응용화학공학부|응용화학공학전공|1
        2025|에너지신소재화학공학부|에너지신소재공학전공|1
        2025|에너지신소재화학공학부|화학생명공학전공|1
        2025|디자인·건축공학부|디자인공학전공|1
        2025|디자인·건축공학부|건축공학전공|1
        2026|컴퓨터공학부|~|0
        2026|기계공학부|~|0
        2026|메카트로닉스공학부|~|0
        2026|전기전자통신공학부|~|0
        2026|고용서비스정책학과|~|0
        2026|응용화학공학부|~|0
        2026|공학융합자율전공|~|0
        2026|ICT융합자율전공|~|0
        2026|사회융합자율전공|~|0
        2026|반도체·디스플레이공학과|~|0
        2026|미래융합학부|~|0
        2026|경영학부|~|0
        2026|디자인공학과|~|0
        2026|건축공학과|~|0
        2026|에너지신소재화학공학부|~|0
        2026|글로벌인재학부|~|0
        2026|기계공학부|~|1
        2026|메카트로닉스공학부|생산시스템전공|1
        2026|메카트로닉스공학부|제어시스템전공|1
        2026|메카트로닉스공학부|디지털시스템전공|1
        2026|전기전자통신공학부|전기공학전공|1
        2026|전기전자통신공학부|전자공학전공|1
        2026|고용서비스정책학과|~|1
        2026|응용화학공학부|응용화학공학전공|1
        2026|컴퓨터공학부|컴퓨터공학전공|1
        2026|컴퓨터공학부|AI·소프트웨어전공|1
        2026|전기전자통신공학부|AI정보통신공학전공|1
        2026|경영학부|융합경영전공|1
        2026|경영학부|데이터경영전공|1
        2026|에너지신소재화학공학부|에너지신소재공학전공|1
        2026|에너지신소재화학공학부|화학생명공학전공|1
        2026|글로벌인재학부|기계공학전공|1
        2026|글로벌인재학부|컴퓨터공학전공|1
        2026|디자인공학과|~|1
        2026|건축공학과|~|1
        """;

    @BeforeEach
    void baseline() {
        flyway("18").clean();
        flyway("18").migrate();
    }

    @Test
    void faulty_75_mappings_become_exactly_62_without_changing_existing_rows() throws Exception {
        List<List<String>> masters;
        List<List<String>> records;
        List<List<String>> retainedMappings;
        try (Connection connection = connect()) {
            seed(connection, FAULTY);
            assertThat(catalog(connection)).hasSize(75);
            seedRecords(connection);
            masters = masters(connection);
            records = records(connection);
            retainedMappings = rows(connection, """
                SELECT a.* FROM academic_program_year a
                JOIN department d ON d.id = a.department_id
                LEFT JOIN major m ON m.id = a.major_id
                WHERE a.student_year = '2024' OR CONCAT(a.student_year, '|', d.name, '|',
                    COALESCE(m.name, '~'), '|', IF(a.major_id IS NULL, '0', '1')) IN (
                """ + expectedSqlValues() + ") ORDER BY a.id");
        }
        flyway("20").migrate();
        try (Connection connection = connect()) {
            assertCatalog(connection);
            assertThat(masters(connection)).containsAll(masters);
            assertThat(records(connection)).isEqualTo(records);
            assertThat(rows(connection, "SELECT * FROM academic_program_year ORDER BY id"))
                .containsAll(retainedMappings);
        }
    }

    @Test
    void manually_corrected_catalog_is_a_no_op_including_ids_metadata_and_timestamps() throws Exception {
        List<List<String>> before;
        try (Connection connection = connect()) {
            seed(connection, EXPECTED);
            seedRecords(connection);
            before = snapshot(connection);
        }
        flyway("20").migrate();
        try (Connection connection = connect()) {
            assertCatalog(connection);
            assertThat(snapshot(connection)).isEqualTo(before);
        }
    }

    @Test
    void empty_baseline_initializes_all_masters_and_direct_script_rerun_is_idempotent() throws Exception {
        flyway("20").migrate();
        try (Connection connection = connect()) {
            assertCatalog(connection);
            assertThat(rows(connection, "SELECT COUNT(*) FROM major WHERE name IS NULL"))
                .containsExactly(List.of("5"));
            assertThat(rows(connection, """
                SELECT COUNT(*) FROM academic_program_year a JOIN major m ON m.id = a.major_id
                WHERE m.name IS NULL
                """)).containsExactly(List.of("7"));
            List<List<String>> before = snapshot(connection);
            ScriptUtils.executeSqlScript(connection, new ClassPathResource(MIGRATION));
            ScriptUtils.executeSqlScript(connection, new ClassPathResource(MIGRATION));
            assertThat(snapshot(connection)).isEqualTo(before);
        }
        // A fresh connection also proves that the migration and direct reruns committed.
        try (Connection connection = connect()) {
            assertCatalog(connection);
            assertThat(rows(connection, "SELECT version FROM flyway_schema_history WHERE version = '20' AND success = 1"))
                .containsExactly(List.of("20"));
        }
    }

    @Test
    void unreviewed_mappings_and_other_years_are_not_deleted() throws Exception {
        List<List<String>> before;
        try (Connection connection = connect()) {
            seed(connection, """
                2024|응용화학공학부|응용화학공학전공|1
                2027|응용화학공학부|응용화학공학전공|1
                2025|검토대상외학부|~|0
                2026|검토대상외학부2026|검토대상외전공|1
                """);
            before = rows(connection, "SELECT * FROM academic_program_year ORDER BY id");
        }
        flyway("20").migrate();
        try (Connection connection = connect()) {
            assertThat(rows(connection, "SELECT * FROM academic_program_year ORDER BY id")).containsAll(before);
            assertThat(catalog(connection)).hasSize(64).containsAll(expected());
            assertThat(rows(connection, "SELECT COUNT(*) FROM academic_program_year WHERE student_year = '2027'"))
                .containsExactly(List.of("1"));
        }
    }

    @Test
    void mapping_insert_failure_rolls_back_master_inserts_and_preserves_old_mappings() throws Exception {
        List<List<String>> before;
        try (Connection connection = connect()) {
            seed(connection, FAULTY);
            before = snapshot(connection);
            String lastDepartmentId = rows(connection, "SELECT MAX(id) FROM department").get(0).get(0);
            execute(connection, "ALTER TABLE academic_program_year ADD CONSTRAINT reject_catalog_insert "
                + "CHECK (department_id <= " + lastDepartmentId + ")");
        }
        assertThatThrownBy(() -> flyway("20").migrate())
            .hasStackTraceContaining("reject_catalog_insert");
        try (Connection connection = connect()) {
            assertThat(snapshot(connection)).isEqualTo(before);
        }
    }

    @Test
    void ambiguous_department_name_fails_before_persistent_changes() throws Exception {
        assertAmbiguous("INSERT INTO department (name) VALUES ('기계공학부'), ('기계공학부')");
    }

    @Test
    void ambiguous_nameless_major_fails_before_persistent_changes() throws Exception {
        try (Connection connection = connect()) {
            execute(connection, "INSERT INTO department (id, name) VALUES (91, '기계공학부')");
        }
        assertAmbiguous("INSERT INTO major (department_id, name) VALUES (91, NULL), (91, NULL)");
    }

    private void assertAmbiguous(String sql) throws Exception {
        List<List<String>> before;
        try (Connection connection = connect()) {
            execute(connection, sql);
            before = snapshot(connection);
        }
        assertThatThrownBy(() -> flyway("20").migrate()).hasStackTraceContaining("Check constraint");
        try (Connection connection = connect()) {
            assertThat(snapshot(connection)).isEqualTo(before);
        }
    }

    private static void assertCatalog(Connection connection) throws SQLException {
        assertThat(catalog(connection)).containsExactlyInAnyOrderElementsOf(expected());
        assertThat(rows(connection, """
            SELECT COUNT(*) FROM academic_program_year a JOIN major m ON m.id = a.major_id
            WHERE a.department_id <> m.department_id
            """)).containsExactly(List.of("0"));
        assertThat(rows(connection, """
            SELECT student_year, COUNT(DISTINCT department_id), COUNT(*)
            FROM academic_program_year WHERE student_year IN ('2025', '2026')
            GROUP BY student_year ORDER BY student_year
            """)).containsExactly(List.of("2025", "11", "26"), List.of("2026", "18", "36"));
        assertThat(rows(connection, "SELECT COUNT(*) FROM academic_program_year WHERE student_year = '2027'"))
            .containsExactly(List.of("0"));
    }

    private static List<String> expected() {
        return EXPECTED.lines().filter(line -> !line.isBlank()).toList();
    }

    private static String expectedSqlValues() {
        return String.join(",", expected().stream().map(value -> "'" + value + "'").toList());
    }

    private static List<String> catalog(Connection connection) throws SQLException {
        return rows(connection, """
            SELECT CONCAT(a.student_year, '|', d.name, '|', COALESCE(m.name, '~'), '|',
                IF(a.major_id IS NULL, '0', '1'))
            FROM academic_program_year a JOIN department d ON d.id = a.department_id
            LEFT JOIN major m ON m.id = a.major_id WHERE a.student_year IN ('2025', '2026')
            """).stream().map(row -> row.get(0)).toList();
    }

    private static void seed(Connection connection, String fixture) throws SQLException {
        for (String line : fixture.lines().filter(value -> !value.isBlank()).toList()) {
            String[] fields = line.split("\\|");
            execute(connection, """
                INSERT INTO department (name, curriculum_link, dept_nums, is_selectable, display_order,
                    created_at, updated_at)
                SELECT ?, 'https://existing.example/curriculum', JSON_ARRAY('existing'), 0, 77,
                    '2020-01-01', '2020-02-01'
                WHERE NOT EXISTS (SELECT 1 FROM department WHERE name = ?)
                """, fields[1], fields[1]);
            String departmentId = rowsWithName(connection, "SELECT id FROM department WHERE name = ?", fields[1]);
            String majorName = fields[2].equals("~") ? null : fields[2];
            String majorId = null;
            if (fields[3].equals("1")) {
                execute(connection, """
                    INSERT INTO major (department_id, name, created_at, updated_at)
                    SELECT ?, ?, '2020-01-01', '2020-02-01' WHERE NOT EXISTS (
                        SELECT 1 FROM major WHERE department_id = ? AND name <=> ?)
                    """, departmentId, majorName, departmentId, majorName);
                try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT id FROM major WHERE department_id = ? AND name <=> ?")) {
                    statement.setString(1, departmentId);
                    statement.setString(2, majorName);
                    try (ResultSet result = statement.executeQuery()) {
                        result.next();
                        majorId = result.getString(1);
                    }
                }
            }
            execute(connection, "INSERT INTO academic_program_year (student_year, department_id, major_id) VALUES (?, ?, ?)",
                fields[0], departmentId, majorId);
        }
    }

    private static String rowsWithName(Connection connection, String sql, String name) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getString(1);
            }
        }
    }

    private static void seedRecords(Connection connection) throws SQLException {
        execute(connection, "INSERT INTO users (id, password, user_type, anonymous_nickname) VALUES (1, 'test', 'STUDENT', 'migration')");
        execute(connection, """
            INSERT INTO students (user_id, student_number, department_id, major_id)
            SELECT 1, '2025135001', department_id, id FROM major ORDER BY id LIMIT 1
            """);
        execute(connection, """
            INSERT INTO standard_graduation_requirements (id, year, major_id, required_grades)
            SELECT 1, '2024', id, 130 FROM major ORDER BY id LIMIT 1
            """);
        execute(connection, "INSERT INTO student_course_calculation (user_id, standard_graduation_requirements_id, completed_grades) VALUES (1, 1, 42)");
        execute(connection, "INSERT INTO detect_graduation_calculation (user_id, is_changed) VALUES (1, 1)");
        execute(connection, """
            INSERT INTO academic_program_year (student_year, department_id, major_id)
            SELECT '2024', department_id, id FROM major ORDER BY id LIMIT 1
            """);
    }

    private static List<List<String>> masters(Connection connection) throws SQLException {
        List<List<String>> result = new ArrayList<>(rows(connection, "SELECT * FROM department ORDER BY id"));
        result.addAll(rows(connection, "SELECT * FROM major ORDER BY id"));
        return result;
    }

    private static List<List<String>> records(Connection connection) throws SQLException {
        List<List<String>> result = new ArrayList<>();
        for (String table : List.of("users", "students", "standard_graduation_requirements",
            "student_course_calculation", "detect_graduation_calculation")) {
            result.addAll(rows(connection, "SELECT * FROM " + table + " ORDER BY 1"));
        }
        return result;
    }

    private static List<List<String>> snapshot(Connection connection) throws SQLException {
        List<List<String>> result = masters(connection);
        result.addAll(records(connection));
        result.addAll(rows(connection, "SELECT * FROM academic_program_year ORDER BY id"));
        return result;
    }

    private static Flyway flyway(String version) {
        return Flyway.configure().dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
            .locations("classpath:db/migration").cleanDisabled(false).target(version).load();
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
    }

    private static void execute(Connection connection, String sql, String... values) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                statement.setString(i + 1, values[i]);
            }
            statement.executeUpdate();
        }
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
