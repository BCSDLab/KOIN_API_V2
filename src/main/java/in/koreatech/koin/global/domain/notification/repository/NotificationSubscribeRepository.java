package in.koreatech.koin.global.domain.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.global.domain.notification.exception.NotificationSubscribeNotFoundException;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;

public interface NotificationSubscribeRepository extends Repository<NotificationSubscribe, Long> {

    NotificationSubscribe save(NotificationSubscribe notificationSubscribe);

    List<NotificationSubscribe> findAllBySubscribeType(NotificationSubscribeType type);

    Optional<NotificationSubscribe> findByUserIdAndSubscribeType(Integer userId, NotificationSubscribeType type);

    default NotificationSubscribe getByUserIdAndSubscribeType(Integer userId, NotificationSubscribeType type) {
        return findByUserIdAndSubscribeType(userId, type)
            .orElseThrow(
                () -> NotificationSubscribeNotFoundException.withDetail("userId: " + userId + ", type: " + type)
            );
    }

    void deleteByUserIdAndSubscribeType(Integer userId, NotificationSubscribeType type);

    List<NotificationSubscribe> findAllByUserId(Integer userId);
}
