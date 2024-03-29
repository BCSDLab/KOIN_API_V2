package in.koreatech.koin.global.domain.notification.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.global.domain.notification.Notification;
import in.koreatech.koin.global.domain.notification.NotificationSubscribe;

public interface NotificationSubscribeRepository extends Repository<NotificationSubscribe, Long> {

    Notification save(Notification notification);

    void deleteByUserIdAndNotificationSubscribeType(Long userId, String type);
}
