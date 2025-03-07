package in.koreatech.koin.integration.fcm.notification.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.integration.fcm.notification.model.Notification;

public interface NotificationRepository extends Repository<Notification, Long> {

    Notification save(Notification notification);
}
