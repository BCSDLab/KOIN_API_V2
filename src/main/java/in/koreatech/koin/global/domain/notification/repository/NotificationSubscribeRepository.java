package in.koreatech.koin.global.domain.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.global.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;

public interface NotificationSubscribeRepository extends Repository<NotificationSubscribe, Long> {

    NotificationSubscribe save(NotificationSubscribe notificationSubscribe);

    List<NotificationSubscribe> findAllBySubscribeTypeAndDetailType(
        NotificationSubscribeType type,
        NotificationDetailSubscribeType detailType
    );

    Optional<NotificationSubscribe> findByDeviceIdAndSubscribeTypeAndDetailType(
        Integer userId,
        NotificationSubscribeType type,
        NotificationDetailSubscribeType detailType
    );

    void deleteByDeviceIdAndSubscribeTypeAndDetailType(
        Integer userId,
        NotificationSubscribeType type,
        NotificationDetailSubscribeType detailType
    );

    List<NotificationSubscribe> findAllByDeviceId(Integer userId);
}
