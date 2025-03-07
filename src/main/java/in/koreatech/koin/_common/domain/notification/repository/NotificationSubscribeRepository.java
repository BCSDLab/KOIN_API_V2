package in.koreatech.koin._common.domain.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin._common.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin._common.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin._common.domain.notification.model.NotificationSubscribeType;

public interface NotificationSubscribeRepository extends Repository<NotificationSubscribe, Long> {

    NotificationSubscribe save(NotificationSubscribe notificationSubscribe);

    List<NotificationSubscribe> findAllBySubscribeTypeAndDetailType(
        NotificationSubscribeType type,
        NotificationDetailSubscribeType detailType
    );

    Optional<NotificationSubscribe> findByUserIdAndSubscribeTypeAndDetailType(
        Integer userId,
        NotificationSubscribeType type,
        NotificationDetailSubscribeType detailType
    );

    void deleteByUserIdAndSubscribeTypeAndDetailType(
        Integer userId,
        NotificationSubscribeType type,
        NotificationDetailSubscribeType detailType
    );

    List<NotificationSubscribe> findAllByUserId(Integer userId);

    List<NotificationSubscribe> findByUserIdAndSubscribeType(
        Integer userId,
        NotificationSubscribeType type
    );

    boolean existsByUserIdAndSubscribeType(Integer userId, NotificationSubscribeType type);
}
