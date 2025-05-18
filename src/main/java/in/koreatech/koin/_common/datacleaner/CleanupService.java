package in.koreatech.koin._common.datacleaner;

import java.time.LocalDateTime;
import java.util.List;

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
    private final List<String> targetTables = List.of(
        "activities", "article_attachments", "boards", "new_articles", "comments", "koin_notice",
        "new_koin_articles", "new_koreatech_articles", "lost_item_articles", "lost_item_images",
        "course_type", "standard_graduation_requirements", "student_course_calculation", "lands",
        "members", "tracks", "tech_stacks", "owner_attachments", "event_articles", "shops",
        "shop_opens", "shop_reviews", "timetable_frame", "timetable_lecture", "users",
        "article_keywords", "article_keyword_user_map"
    );

    @Transactional
    public void cleanupSoftDeletedData() {
        LocalDateTime expirationDate = LocalDateTime.now().minusDays(RETENTION_DAYS);

        for (String table : targetTables) {
            String sql = String.format(
                "DELETE FROM %s WHERE is_deleted = 1 AND updated_at < :expirationDate", table
            );

            int deletedCount = entityManager.createNativeQuery(sql)
                .setParameter("expirationDate", expirationDate)
                .executeUpdate();

            log.info("{}: {}개 행 삭제 완료", table, deletedCount);
        }
    }
}
