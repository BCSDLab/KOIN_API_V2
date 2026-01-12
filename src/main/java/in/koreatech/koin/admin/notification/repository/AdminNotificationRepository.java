package in.koreatech.koin.admin.notification.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.notification.model.Notification;

public interface AdminNotificationRepository extends Repository<Notification, Integer> {

    void save(Notification notification);
}
