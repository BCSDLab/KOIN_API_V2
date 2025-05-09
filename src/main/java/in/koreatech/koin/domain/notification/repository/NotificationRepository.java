package in.koreatech.koin.domain.notification.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.notification.model.Notification;

public interface NotificationRepository extends Repository<Notification, Long> {

    Notification save(Notification notification);
}
