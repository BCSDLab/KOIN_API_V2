package in.koreatech.koin.domain.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface NotificationSubscribeRepository extends Repository<NotificationSubscribe, Long> {

    NotificationSubscribe save(NotificationSubscribe notificationSubscribe);

    // TODO : Service 메서드로 네이밍 변경
    List<NotificationSubscribe> findAllBySubscribeTypeAndDetailTypeIsNull(NotificationSubscribeType type);

    boolean existsByUserIdAndSubscribeTypeAndDetailTypeIsNull(Integer userId, NotificationSubscribeType type);

    boolean existsByUserIdAndSubscribeTypeAndDetailType(
        Integer userId,
        NotificationSubscribeType type,
        NotificationDetailSubscribeType detailType
    );

    void deleteByUserIdAndSubscribeTypeAndDetailTypeIsNull(
        Integer userId,
        NotificationSubscribeType type
    );

    void deleteByUserIdAndDetailType(
        Integer userId,
        NotificationDetailSubscribeType detailType
    );

    List<NotificationSubscribe> findAllByUserId(Integer userId);

    @Query("""
        SELECT DISTINCT ns
        FROM NotificationSubscribe ns
        JOIN FETCH ns.user u
        WHERE ns.subscribeType = :subscribeType
        AND ns.detailType IS NULL
        AND EXISTS (
            SELECT 1 FROM NotificationSubscribe ns2
            WHERE ns2.user.id = ns.user.id
            AND ns2.subscribeType = :subscribeType
            AND ns2.detailType = :detailType
        )
        AND u.deviceToken IS NOT NULL
        """)
    List<NotificationSubscribe> findAllBySubscribeTypeAndDetailType(
        @Param("subscribeType") NotificationSubscribeType subscribeType,
        @Param("detailType") NotificationDetailSubscribeType detailType
    );
}
