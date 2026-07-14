package in.koreatech.koin.acceptance.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class DepartmentContactMigrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.29")
        .withDatabaseName("department_contact_migration")
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
    void 부서_연락처_마이그레이션과_초기_데이터를_검증한다() throws SQLException {
        try (Connection connection = DriverManager.getConnection(
            MYSQL.getJdbcUrl(),
            MYSQL.getUsername(),
            MYSQL.getPassword()
        )) {
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM department_contact_departments"))
                .isEqualTo(54);
            assertThat(queryInt(connection, "SELECT COUNT(DISTINCT category) FROM department_contact_departments"))
                .isEqualTo(6);
            assertThat(queryInt(connection, "SELECT COUNT(*) FROM department_contacts"))
                .isEqualTo(108);
            assertThat(queryInt(connection, """
                SELECT COUNT(*)
                FROM department_contact_departments
                WHERE name <> TRIM(name)
                """))
                .isZero();
            assertThat(queryString(connection, displayOrderTypeQuery("department_contact_departments")))
                .isEqualTo("int");
            assertThat(queryString(connection, displayOrderTypeQuery("department_contacts")))
                .isEqualTo("int");
            assertThat(queryString(connection, phoneNumberQuery("혁신지원사업운영팀")))
                .isEqualTo("041-640-8640");
            assertThat(queryString(connection, phoneNumberQuery("기술창업혁신팀")))
                .isEqualTo("041-580-4802");
        }
    }

    private String displayOrderTypeQuery(String tableName) {
        return """
            SELECT DATA_TYPE
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
                AND TABLE_NAME = '%s'
                AND COLUMN_NAME = 'display_order'
            """.formatted(tableName);
    }

    private String phoneNumberQuery(String departmentName) {
        return """
            SELECT contacts.phone_number
            FROM department_contacts AS contacts
            JOIN department_contact_departments AS departments
                ON departments.id = contacts.department_id
            WHERE departments.name = '%s'
            """.formatted(departmentName);
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
