package in.koreatech.koin.admin.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.domain.notification.model.NotificationSubscribeType;

public interface AdminNotificationSubscribeRepository extends Repository<NotificationSubscribe, Long> {

    @Query("""
        SELECT ns
        FROM NotificationSubscribe ns
        JOIN FETCH ns.user u
        WHERE ns.subscribeType = :subscribeType
        AND ns.detailType IS NULL
        AND u.deviceToken IS NOT NULL
        """)
    List<NotificationSubscribe> findAllBySubscribeType(
        @Param("subscribeType") NotificationSubscribeType subscribeType
    );

    @Query("""
        SELECT ns
        FROM NotificationSubscribe ns
        JOIN FETCH ns.user u
        WHERE ns.subscribeType = :subscribeType
        AND ns.detailType = :detailType
        AND u.deviceToken IS NOT NULL
        """)
    List<NotificationSubscribe> findAllBySubscribeTypeAndDetailType(
        @Param("subscribeType") NotificationSubscribeType subscribeType,
        @Param("detailType") NotificationDetailSubscribeType detailType
    );
}
