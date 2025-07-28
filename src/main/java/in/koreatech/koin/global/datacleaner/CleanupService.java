package in.koreatech.koin.global.datacleaner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupService {

    private final EntityManager entityManager;
    private final CleanupProperties cleanupProperties;

    @Transactional
    public void cleanupSoftDeletedData() {
        int retentionDays = Optional.ofNullable(cleanupProperties.retentionDays()).orElse(60);
        LocalDateTime expirationDate = LocalDateTime.now().minusDays(retentionDays);
        List<String> targetTables = cleanupProperties.targetTables();
        if (targetTables == null || targetTables.isEmpty()) {
            log.warn("No target tables configured for cleanup.");
            return;
        }

        for (String table : targetTables) {
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
