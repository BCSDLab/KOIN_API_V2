package in.koreatech.koin.domain.community.keyword.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.community.keyword.model.UserNotificationStatus;

public interface UserNotificationStatusRepository extends Repository<UserNotificationStatus, Integer> {

    void save(UserNotificationStatus status);

    Optional<UserNotificationStatus> findByUserId(Integer userId);

    boolean existsByNotifiedArticleIdAndUserId(Integer notifiedArticleId, Integer userId);

    @Query("""
        SELECT status.userId
        FROM UserNotificationStatus status
        WHERE status.notifiedArticleId = :notifiedArticleId
        AND status.userId IN :userIds
        """)
    List<Integer> findUserIdsByNotifiedArticleIdAndUserIdIn(
        @Param("notifiedArticleId") Integer notifiedArticleId,
        @Param("userIds") Collection<Integer> userIds
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
        INSERT INTO user_notification_status (user_id, last_notified_article_id, created_at, updated_at)
        VALUES (:userId, :notifiedArticleId, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON DUPLICATE KEY UPDATE
            last_notified_article_id = :notifiedArticleId,
            updated_at = CURRENT_TIMESTAMP
        """, nativeQuery = true)
    void upsertLastNotifiedArticleId(
        @Param("userId") Integer userId,
        @Param("notifiedArticleId") Integer notifiedArticleId
    );
}
