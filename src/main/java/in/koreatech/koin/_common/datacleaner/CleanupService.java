package in.koreatech.koin._common.datacleaner;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupService {

    private static final int RETENTION_DAYS = 60;

    private final EntityManager entityManager;
    private final CleanupProperties cleanupProperties;

    @Transactional
    public void cleanupSoftDeletedData() {
        LocalDateTime expirationDate = LocalDateTime.now().minusDays(RETENTION_DAYS);

        for (String table : cleanupProperties.targetTables()) {
            String sql = String.format(
                "DELETE FROM %s WHERE is_deleted = 1 AND updated_at < :expirationDate", table
            );
            int deletedCount = entityManager.createNativeQuery(sql)
                .setParameter("expirationDate", expirationDate)
                .executeUpdate();

            log.info("deleted {}: {}rows", table, deletedCount);
        }
    }
}
