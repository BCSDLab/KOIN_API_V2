package in.koreatech.koin.global.domain.notification;

import org.springframework.data.repository.Repository;

public interface NotificationRepository extends Repository<Notification, Long> {

    Notification save(Notification notification);
}
