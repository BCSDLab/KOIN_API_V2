package in.koreatech.koin.integration.notification.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.integration.notification.model.Notification;

public interface NotificationRepository extends Repository<Notification, Long> {

    Notification save(Notification notification);
}
