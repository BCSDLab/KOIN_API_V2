package in.koreatech.koin.acceptance.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class LandRentTypeAvailabilityMigrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.29")
        .withDatabaseName("land_rent_type_migration")
        .withUsername("test")
        .withPassword("test");

    // 금액 값 → V28 백필 뒤 기대하는 가능 여부. 공백 문자만 있으면 금액이 없는 것으로 본다.
    private static final Map<String, Boolean> FEES = new LinkedHashMap<>();

    static {
        FEES.put(null, false);
        FEES.put("", false);
        FEES.put("   ", false);
        FEES.put("\t", false);
        FEES.put("\n", false);
        FEES.put(" \t\r\n ", false);
        FEES.put("　", false);
        FEES.put(" ", false);
        FEES.put("30", true);
        FEES.put(" 30 ", true);
        FEES.put("\t5000\n", true);
        FEES.put("문의", true);
    }

    @BeforeAll
    static void migrate() throws Exception {
        Flyway.configure().dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
            .locations("classpath:db/migration").target("27").load().migrate();
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO lands (name, internal_name, monthly_fee, charter_fee) VALUES (?, ?, ?, ?)")) {
            int index = 0;
            for (String fee : FEES.keySet()) {
                statement.setString(1, "건물" + index);
                statement.setString(2, "건물" + index++);
                statement.setString(3, fee);
                statement.setString(4, fee);
                statement.executeUpdate();
            }
        }
        Flyway.configure().dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
            .locations("classpath:db/migration").target("28").load().migrate();
    }

    @Test
    void V28은_금액에_공백이_아닌_글자가_있을_때만_월세_전세_가능으로_옮긴다() throws Exception {
        Map<String, Boolean> monthly = new LinkedHashMap<>();
        Map<String, Boolean> charter = new LinkedHashMap<>();
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("""
            SELECT monthly_fee, is_monthly_available, charter_fee, is_charter_available FROM lands ORDER BY id
            """); ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                monthly.put(result.getString(1), result.getBoolean(2));
                charter.put(result.getString(3), result.getBoolean(4));
            }
        }
        assertThat(describe(monthly)).isEqualTo(describe(FEES));
        assertThat(describe(charter)).isEqualTo(describe(FEES));
    }

    // 실패 메시지에서 공백 문자가 보이도록 코드값으로 바꿔 비교한다.
    private static Map<String, Boolean> describe(Map<String, Boolean> fees) {
        Map<String, Boolean> described = new LinkedHashMap<>();
        fees.forEach((fee, available) -> described.put(
            fee == null ? "NULL" : Arrays.toString(fee.codePoints().toArray()), available));
        return described;
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
    }
}
