package in.koreatech.koin.support;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@TestComponent
public class DBInitializer {

    private static final int OFF = 0;
    private static final int ON = 1;
    private static final int COLUMN_INDEX = 1;

    private List<String> tableNames = new ArrayList<>();

    @Autowired
    private DataSource dataSource;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private RedisTemplate<?, ?> redisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    private void findDatabaseTableNames() {
        String sql = "SHOW TABLES";
        tableNames = entityManager.createNativeQuery(sql).getResultList();
    }

    private void truncateAllTable() {
        setForeignKeyCheck(OFF);
        for (String tableName: tableNames) {
            entityManager.createNativeQuery(String.format("TRUNCATE TABLE %s", tableName)).executeUpdate();
        }
        setForeignKeyCheck(ON);
    }

    @Transactional
    public void initIncrement() {
        String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'test' AND AUTO_INCREMENT >= 1";
        List<String> dirtyTables = entityManager.createNativeQuery(sql).getResultList();
        for (String tableName: dirtyTables) {
            entityManager.createNativeQuery(String.format("ALTER TABLE %s AUTO_INCREMENT = 1", tableName)).executeUpdate();
        }
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
        truncateAllTable();
        clearRedis();
        clearMongo();
    }

    public void clearRedis() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    private void clearMongo() {
        mongoTemplate.getCollectionNames().forEach(collectionName -> mongoTemplate.remove(new Query(), collectionName));
    }
}
