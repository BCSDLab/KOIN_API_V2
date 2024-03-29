package in.koreatech.koin.global.domain.notification.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.global.domain.notification.NotificationSubscribe;
import in.koreatech.koin.global.domain.notification.NotificationSubscribeType;

public interface NotificationSubscribeRepository extends Repository<NotificationSubscribe, Long> {

    NotificationSubscribe save(NotificationSubscribe notificationSubscribe);

    Optional<NotificationSubscribe> findByUserIdAndSubscribeType(Long userId, NotificationSubscribeType type);

    default NotificationSubscribe getByUserIdAndSubscribeType(Long userId, NotificationSubscribeType type) {
        return findByUserIdAndSubscribeType(userId, type)
            .orElseThrow(() -> NotificationSubscribeNotFoundException.withDetail("userId: " + userId + ", type: " + type);
    }

    void deleteByUserIdAndSubscribeType(Long userId, NotificationSubscribeType type);
}
