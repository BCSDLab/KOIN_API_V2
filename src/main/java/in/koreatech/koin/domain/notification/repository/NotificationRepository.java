package in.koreatech.koin.domain.notification.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface NotificationRepository extends Repository<Notification, Long> {

    Notification save(Notification notification);
}
