package in.koreatech.koin.acceptance.support;

import java.util.ArrayList;
import java.util.List;

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

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private RedisTemplate<?, ?> redisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    private List<String> tableNames = new ArrayList<>();

    private void findDatabaseTableNames() {
        String sql = "SHOW TABLES";
        tableNames = entityManager.createNativeQuery(sql).getResultList();
    }

    private void truncateDirtyTables() {
        if (tableNames.isEmpty()) {
            findDatabaseTableNames();
        }

        setForeignKeyCheck(OFF);
        for (String tableName : tableNames) {
            long count = ((Number) entityManager
                .createNativeQuery(String.format("SELECT COUNT(*) FROM `%s`", tableName))
                .getSingleResult()).longValue();

            if (count > 0) {
                entityManager.createNativeQuery(String.format("TRUNCATE TABLE `%s`", tableName)).executeUpdate();
            }
        }
        setForeignKeyCheck(ON);
    }

    @Transactional
    public void initIncrement() {
        String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'test' AND AUTO_INCREMENT > 1";
        List<String> dirtyTables = entityManager.createNativeQuery(sql).getResultList();
        for (String tableName : dirtyTables) {
            entityManager.createNativeQuery(String.format("ALTER TABLE %s AUTO_INCREMENT = 1", tableName))
                .executeUpdate();
        }
    }

    private void setForeignKeyCheck(int mode) {
        entityManager.createNativeQuery(String.format("SET FOREIGN_KEY_CHECKS = %d", mode)).executeUpdate();
    }

    @Transactional
    public void clear() {
        entityManager.clear();
        truncateDirtyTables();
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
