package in.koreatech.koin.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.transaction.annotation.Transactional;

@TestComponent
public class DBInitializer {

    private static final int OFF = 0;
    private static final int ON = 1;
    private static final int COLUMN_INDEX = 1;

    private final List<String> tableNames = new ArrayList<>();

    @Autowired
    private DataSource dataSource;

    @PersistenceContext
    private EntityManager entityManager;

    private void findDatabaseTableNames() {
        try (final Statement statement = dataSource.getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery("SHOW TABLES");
            while (resultSet.next()) {
                final String tableName = resultSet.getString(COLUMN_INDEX);
                tableNames.add(tableName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void truncate() {
        setForeignKeyCheck(OFF);
        for (String tableName : tableNames) {
            entityManager.createNativeQuery(String.format("TRUNCATE TABLE %s", tableName)).executeUpdate();
        }
        setForeignKeyCheck(ON);
    }

    private void setForeignKeyCheck(int mode) {
        entityManager.createNativeQuery(String.format("SET FOREIGN_KEY_CHECKS = %d", mode)).executeUpdate();
    }

    @Transactional
    public void clear() {
        if (tableNames.isEmpty()) {
            findDatabaseTableNames();
        }
        entityManager.clear();
        truncate();
    }
}
