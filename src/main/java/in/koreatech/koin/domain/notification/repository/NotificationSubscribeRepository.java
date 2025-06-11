package in.koreatech.koin.domain.notification.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.domain.notification.model.NotificationSubscribeType;

public interface NotificationSubscribeRepository extends Repository<NotificationSubscribe, Long> {

    NotificationSubscribe save(NotificationSubscribe notificationSubscribe);

    List<NotificationSubscribe> findAllBySubscribeTypeAndDetailTypeIsNull(NotificationSubscribeType type);

    boolean existsByUserIdAndSubscribeType(Integer userId, NotificationSubscribeType type);

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
}
