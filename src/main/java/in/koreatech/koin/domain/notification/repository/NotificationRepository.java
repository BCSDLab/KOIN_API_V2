package in.koreatech.koin.domain.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.notification.model.Notification;

public interface NotificationRepository extends Repository<Notification, Long> {

    Notification save(Notification notification);

    void saveAll(Iterable<Notification> notifications);

    @Query("""
        SELECT DISTINCT n.user.id
        FROM Notification n
        WHERE n.schemeUri LIKE :schemeUriPattern
        AND n.user.id IN :userIds
        """)
    List<Integer> findUserIdsBySchemeUriLikeAndUserIdIn(
        @Param("schemeUriPattern") String schemeUriPattern,
        @Param("userIds") List<Integer> userIds
    );
}
