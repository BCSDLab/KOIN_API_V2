package in.koreatech.koin.global.domain.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.global.domain.notification.exception.NotificationSubscribeNotFoundException;
import in.koreatech.koin.global.domain.notification.model.NotificationDetailSubscribe;
import in.koreatech.koin.global.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;

public interface NotificationDetailSubscribeRepository extends Repository<NotificationDetailSubscribe, Integer> {

    NotificationDetailSubscribe save(NotificationDetailSubscribe notificationDetailSubscribe);

    List<NotificationDetailSubscribe> findAllBySubscribeType(NotificationSubscribeType subscribeType);

    Optional<NotificationDetailSubscribe> findByUserIdAndDetailSubscribeType(Integer userId,
        NotificationDetailSubscribeType detailType);

    default NotificationDetailSubscribe getByUserIdAndDetailSubscribeType(Integer userId,
        NotificationDetailSubscribeType detailType) {
        return findByUserIdAndDetailSubscribeType(userId, detailType)
            .orElseThrow(
                () -> NotificationSubscribeNotFoundException.withDetail(
                    "userId: " + userId + ", detailType: " + detailType)
            );
    }

    void deleteByUserIdAndDetailSubscribeType(Integer userId, NotificationDetailSubscribeType type);

    List<NotificationDetailSubscribe> findAllByUserId(Integer userId);
}
